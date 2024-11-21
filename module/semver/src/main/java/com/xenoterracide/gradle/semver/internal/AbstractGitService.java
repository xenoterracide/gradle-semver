// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import com.xenoterracide.gradle.semver.internal.AbstractGitService.Params;
import io.vavr.control.Try;
import java.io.IOException;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.SystemReader;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.jspecify.annotations.Nullable;

/**
 * Build Service for Git. Primary goal is to allow for lazy initialization of the Git object and keeping it open for
 * later usage. This Service should not be considered a published API, and may change or be removed in future versions.
 */
public abstract class AbstractGitService implements BuildService<Params>, AutoCloseable {
  static {
    preventJGitFromCallingExecutables();
  }

  private @Nullable Git git;

  /**
   * Constructor for the Git Service.
   */
  @Inject
  @SuppressWarnings({ "this-escape", "InjectOnConstructorOfAbstractClass" })
  public AbstractGitService() {}

  // preventJGitFromCallingExecutables is copied from
  // https://github.com/diffplug/spotless/blob/224f8f96df3ad42cac81064a0461e6d4ee91dcaf/plugin-gradle/src/main/java/com/diffplug/gradle/spotless/GitRatchetGradle.java#L35
  // SPDX-License-Identifier: Apache-2.0
  // Copyright 2020-2023 DiffPlug
  static void preventJGitFromCallingExecutables() {
    SystemReader reader = SystemReader.getInstance();
    SystemReader.setInstance(
      new SystemReader.Delegate(reader) {
        @Override
        public String getenv(String variable) {
          if ("PATH".equals(variable)) {
            return "";
          } else {
            return super.getenv(variable);
          }
        }
      }
    );
  }

  Optional<Git> lazyGit() throws IOException {
    if (this.git == null) {
      var projectDir = this.getParameters().getProjectDirectory().get().getAsFile();
      var gitDir = new FileRepositoryBuilder().readEnvironment().setMustExist(false).findGitDir(projectDir).getGitDir();

      this.git = gitDir != null ? Git.open(gitDir) : null;
    }

    return Optional.ofNullable(this.git);
  }

  /**
   * Create the SemverExtension.
   *
   * @return The SemverExtension.
   */
  public GitMetadata metadata() {
    return new GitMetadataImpl(() -> Try.of(this::lazyGit).getOrElse(Optional.empty()));
  }

  @Override
  public void close() {
    if (this.git != null) this.git.close();
  }

  /**
   * Parameters for the Git Service.
   */
  public interface Params extends BuildServiceParameters {
    /**
     * The project directory.
     *
     * @return The project directory.
     */
    DirectoryProperty getProjectDirectory();
  }
}

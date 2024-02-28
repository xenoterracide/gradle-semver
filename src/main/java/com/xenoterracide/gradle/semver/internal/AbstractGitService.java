// SPDX-License-Identifier: Apache-2.0
// © Copyright 2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver.internal;

import com.xenoterracide.gradle.semver.SemverExtension;
import com.xenoterracide.gradle.semver.internal.AbstractGitService.Params;
import io.vavr.control.Try;
import java.io.IOException;
import javax.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
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
  private @Nullable Repository repository;

  @Inject
  @SuppressWarnings({ "this-escape", "InjectOnConstructorOfAbstractClass" })
  public AbstractGitService() {}

  Git lazyGit() throws IOException {
    if (this.git == null) {
      var builder = new FileRepositoryBuilder()
        .readEnvironment()
        .setMustExist(true)
        .findGitDir(this.getParameters().getProjectDirectory().get().getAsFile());
      this.repository = builder.build();
      this.git = new Git(this.repository);
    }

    return this.git;
  }

  public SemverExtension extension() {
    return new SemverExtension(() -> Try.of(this::lazyGit).onFailure(ExceptionTools::rethrow).get());
  }

  @Override
  public void close() {
    if (this.git != null) this.git.close();
    if (this.repository != null) this.repository.close();
  }

  // preventJGitFromCallingExecutables is copied from
  // https://github.com/diffplug/spotless/blob/224f8f96df3ad42cac81064a0461e6d4ee91dcaf/plugin-gradle/src/main/java/com/diffplug/gradle/spotless/GitRatchetGradle.java#L35
  // SPDX-License-Identifier: Apache-2.0
  // Copyright 2020-2023 DiffPlug
  static void preventJGitFromCallingExecutables() {
    SystemReader reader = SystemReader.getInstance();
    SystemReader.setInstance(
      new DelegatingSystemReader(reader) {
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

  public interface Params extends BuildServiceParameters {
    DirectoryProperty getProjectDirectory();
  }
}

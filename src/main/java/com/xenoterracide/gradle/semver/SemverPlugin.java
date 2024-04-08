// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import io.vavr.control.Try;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.SystemReader;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Pure Java, configuration cache safe semantic versioning with git plugin for gradle.
 */
public class SemverPlugin implements Plugin<Project> {
  static {
    preventJGitFromCallingExecutables();
  }

  private static final String SEMVER = "semver";

  /**
   * Instantiates a new Semver plugin.
   */
  public SemverPlugin() {}

  // preventJGitFromCallingExecutables is copied from
  // https://github.com/diffplug/spotless/blob/224f8f96df3ad42cac81064a0461e6d4ee91dcaf/plugin-gradle/src/main/java/com/diffplug/gradle/spotless/GitRatchetGradle.java#L35
  // SPDX-License-Identifier: Apache-2.0
  // Copyright 2020-2023 DiffPlug
  static void preventJGitFromCallingExecutables() {
    var reader = SystemReader.getInstance();
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

  @Override
  public void apply(Project project) {
    project
      .getExtensions()
      .add(
        SEMVER,
        new SemverExtension(
          Try.withResources(() -> {
            var currentDir = project.getLayout().getProjectDirectory().getAsFile();
            var builder = new FileRepositoryBuilder().readEnvironment().setMustExist(false).findGitDir(currentDir);

            try (var repo = builder.build()) {
              // doing it this way because Jgit won't cloase all resources if you create it from the built version
              return repo.getFS() != null ? Git.open(repo.getWorkTree()) : null;
            }
          })
        )
      );
  }
}

// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import io.vavr.control.Try;
import org.eclipse.jgit.util.SystemReader;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SemVerPlugin implements Plugin<Project> {

  static final String EXTENSION = "semver";

  static {
    preventJGitFromCallingExecutables();
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
          return "PATH".equals(variable) ? "" : super.getenv(variable);
        }
      }
    );
  }

  @Override
  public void apply(Project project) {
    var serviceProvider = project
      .getGradle()
      .getSharedServices()
      .registerIfAbsent(
        "gitService",
        AbstractGitService.class,
        spec -> {
          spec.getParameters().getProjectDirectory().set(project.getLayout().getProjectDirectory());
        }
      );

    project
      .getExtensions()
      .add(
        EXTENSION,
        serviceProvider.map(s -> Try.of(s::getPorcelainGit).getOrElseThrow(ExceptionTools::rethrow)).get().getSemver()
      );
  }
}

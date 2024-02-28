// SPDX-License-Identifier: Apache-2.0
// © Copyright 2018-2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.AbstractGitService;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SemverPlugin implements Plugin<Project> {

  private static final String SEMVER = "semver";

  @Override
  public void apply(Project project) {
    var svcPrvdr = project
      .getGradle()
      .getSharedServices()
      .registerIfAbsent("gitService", AbstractGitService.class, spec -> {
        spec.getParameters().getProjectDirectory().set(project.getLayout().getProjectDirectory());
      });

    project.getExtensions().add(SEMVER, svcPrvdr.map(AbstractGitService::extension).get());
  }
}

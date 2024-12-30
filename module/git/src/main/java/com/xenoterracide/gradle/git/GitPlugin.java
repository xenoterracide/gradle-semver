// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GitPlugin implements Plugin<Project> {

  private static final String GIT = "git";

  @Override
  public void apply(Project project) {
    var gitDir = project
      .getProviders()
      .of(GitDirectoryValueSource.class, c ->
        c.parameters(p -> {
          p.getProjectDirectory().set(project.getLayout().getProjectDirectory());
        })
      );

    var gitService = project
      .getGradle()
      .getSharedServices()
      .registerIfAbsent(GitService.class.getCanonicalName(), GitService.class, spec -> {
        spec.getParameters().getGitDirectory().fileProvider(gitDir);
      });

    project.getExtensions().add(GIT, new GitExtension(gitService));
  }
}

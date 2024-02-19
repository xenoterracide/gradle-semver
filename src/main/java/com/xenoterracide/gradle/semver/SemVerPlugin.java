// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SemVerPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getExtensions().add("gitVersion", new GitVersionProvider(project.getProjectDir()));
  }
}

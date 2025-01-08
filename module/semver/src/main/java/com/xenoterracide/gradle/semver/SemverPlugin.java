// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.GitPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Pure Java, configuration cache safe semantic versioning with git plugin for gradle.
 */
public class SemverPlugin implements Plugin<Project> {

  private static final String SEMVER = "semver";

  /**
   * Instantiates a new Semver plugin.
   */
  public SemverPlugin() {}

  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(GitPlugin.class);
    project.getExtensions().add(SEMVER, SemverExtension.forProject(project));
  }
}

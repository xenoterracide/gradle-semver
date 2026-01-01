// SPDX-FileCopyrightText: Copyright © 2024 - 2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.GitPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.semver4j.Semver;

/**
 * Pure Java, configuration cache safe semantic versioning with git plugin for gradle.
 */
public class SemverPlugin implements Plugin<Project> {

  private static final String GROUP = "Help";
  private static final String SEMVER = "semver";

  /**
   * Instantiates a new Semver plugin.
   */
  public SemverPlugin() {}

  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(GitPlugin.class);

    var semver = SemverExtension.forProject(project);
    project.getExtensions().add(SEMVER, semver);

    var tasks = project.getTasks();

    tasks.register("semverVersion", PrintVersionTask.class, task -> {
      task.setGroup(GROUP);
      task.setDescription("Prints the semantic version computed by the semver plugin");
      task.getVersionText().set(semver.getProvider().map(Object::toString).orElse(Semver.ZERO.toString()));
    });

    tasks.register("version", PrintVersionTask.class, task -> {
      task.setGroup(GROUP);
      task.setDescription("Prints project.version");
      task.getVersionText().set(project.provider(() -> project.getVersion().toString()));
    });
  }
}

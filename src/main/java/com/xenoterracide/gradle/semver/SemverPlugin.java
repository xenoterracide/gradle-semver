// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.AbstractGitService;
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
    var svcPrvdr = project
      .getGradle()
      .getSharedServices()
      .registerIfAbsent(AbstractGitService.class.getCanonicalName(), AbstractGitService.class, spec -> {
        spec.getParameters().getProjectDirectory().set(project.getLayout().getProjectDirectory());
      });

    project.getExtensions().add(SEMVER, new SemverExtension(svcPrvdr.map(AbstractGitService::metadata)::get));
    project
      .getTasks()
      .register("version", task -> {
        task.setDescription("Prints the current project version.");
        task.doLast(t -> System.out.println(project.getVersion()));
      });
  }
}

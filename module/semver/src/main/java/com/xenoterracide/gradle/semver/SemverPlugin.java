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

    var extensions = project.getExtensions();
    extensions.add("semver", project.provider(svcPrvdr.map(AbstractGitService::extension).get()::getVersion));
    extensions.add("git", svcPrvdr.map(AbstractGitService::extension).get().getGit());

    project
      .getTasks()
      .register("version", task -> {
        task.setDescription("Prints the current project version.");
        task.doLast(t -> System.out.println(task.getProject().getVersion()));
      });
  }
}

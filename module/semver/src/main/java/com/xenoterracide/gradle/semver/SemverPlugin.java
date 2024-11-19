// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.AbstractGitService;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;
import org.semver4j.Semver;

/**
 * Pure Java, configuration cache safe semantic versioning with git plugin for gradle.
 */
public class SemverPlugin implements Plugin<Project> {

  private static final String SEMVER = "semver";
  private static final String GIT = "gitMetadata";

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

    var gitMeta = svcPrvdr.map(AbstractGitService::extension).get();
    project.getExtensions().add(GIT, gitMeta);

    var semverProvider = project.getProviders().provider(() -> new SemverBuilder(gitMeta).build());
    var semverProperty = project.getObjects().property(Semver.class);
    semverProperty.set(semverProvider);
    semverProperty.disallowChanges();
    semverProperty.finalizeValueOnRead();

    project.getExtensions().add(new TypeOf<Provider<Semver>>() {}, SEMVER, semverProperty);
  }
}

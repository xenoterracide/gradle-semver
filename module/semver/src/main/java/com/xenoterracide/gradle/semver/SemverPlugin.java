// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.GitMetadataImpl;
import com.xenoterracide.gradle.semver.internal.GitService;
import com.xenoterracide.gradle.semver.internal.ProvidedFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

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
    var tryGit = project
      .getGradle()
      .getSharedServices()
      .registerIfAbsent(GitService.class.getCanonicalName(), GitService.class, spec -> {
        spec.getParameters().getProjectDirectory().set(project.getLayout().getProjectDirectory());
      });

    project
      .getExtensions()
      .add(GIT, new GitMetadataExtension(new ProvidedFactory(project), new GitMetadataImpl(tryGit::get)));
    project.getExtensions().add(SEMVER, new SemverExtension(project).init(tryGit::get));
  }
}

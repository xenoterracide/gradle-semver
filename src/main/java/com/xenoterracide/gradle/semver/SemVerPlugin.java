// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.
package com.xenoterracide.gradle.semver;

import java.io.IOException;
import java.util.Optional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.lib.BaseRepositoryBuilder;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * inspired by @see <a href="https://github.com/cinnober/semver-git/">Cinnober's SemVer Git</a>
 */
public class SemVerPlugin implements Plugin<Project> {

  private final Logger log = LoggerFactory.getLogger(SemVerPlugin.class);

  @Override
  public void apply(Project project) {
    BaseRepositoryBuilder<?, ?> builder = new FileRepositoryBuilder().findGitDir(project.getProjectDir());
    try (var repo = builder.build()) {
      Optional
        .ofNullable(new PorcelainGit(new Git(repo)).describe())
        .map(v -> v.substring(1))
        .map(v -> v.contains("g") ? v + "-SNAPSHOT" : v)
        .ifPresent(project::setVersion);
    } catch (IOException | InvalidPatternException | GitAPIException e) {
      log.error("", e);
    }
  }
}

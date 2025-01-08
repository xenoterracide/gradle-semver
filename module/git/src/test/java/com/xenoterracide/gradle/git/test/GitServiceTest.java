// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.xenoterracide.gradle.git.GitPlugin;
import com.xenoterracide.gradle.git.GitService;
import java.io.File;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

class GitServiceTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File projectDir;

  @Test
  void noGit() {
    var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
    project.getPluginManager().apply(GitPlugin.class);
    var provider = project
      .getGradle()
      .getSharedServices()
      .registerIfAbsent(GitService.class.getCanonicalName(), GitService.class);
    try (var svc = provider.get()) {
      assertThat(svc).isNotNull();
      assertThat(svc.getProvider().getOrNull()).isNull();
    }
  }

  @Test
  void git() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      assertThat(git).isNotNull();
    }
    var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
    project.getPluginManager().apply(GitPlugin.class);
    var provider = project
      .getGradle()
      .getSharedServices()
      .registerIfAbsent(GitService.class.getCanonicalName(), GitService.class);

    try (var svc = provider.get()) {
      assertThat(svc).isNotNull();
      assertThat(svc.getProvider().getOrNull()).isNotNull();
    }
  }
}

// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class GitRemoteForGradleTest {

  @Test
  void delegatesToRemote() {
    var project = ProjectBuilder.builder().build();
    var pf = new ProvidedFactory(project);

    GitRemote remote = new GitRemote() {
      @Override
      public String name() {
        return "origin";
      }

      @Override
      public String headBranch() {
        return "main";
      }

      @Override
      public String headBranchRefName() {
        return "refs/remotes/origin/main";
      }
    };

    var gitRemoteForGradle = new GitRemoteForGradle(pf, remote);

    assertThat(gitRemoteForGradle.name()).isEqualTo("origin");
    assertThat(gitRemoteForGradle.headBranch()).isEqualTo("main");
    assertThat(gitRemoteForGradle.headBranchRefName()).isEqualTo("refs/remotes/origin/main");
    assertThat(gitRemoteForGradle.getName()).isEqualTo("origin");
    assertThat(gitRemoteForGradle.getHeadBranch().get()).isEqualTo("main");
    assertThat(gitRemoteForGradle.getHeadBranchRefName().get()).isEqualTo("refs/remotes/origin/main");
  }

  @Test
  void handlesNullHeadBranch() {
    var project = ProjectBuilder.builder().build();
    var pf = new ProvidedFactory(project);

    GitRemote remote = new GitRemote() {
      @Override
      public String name() {
        return "origin";
      }

      @Override
      public String headBranch() {
        return null;
      }

      @Override
      public String headBranchRefName() {
        return null;
      }
    };

    var gitRemoteForGradle = new GitRemoteForGradle(pf, remote);

    assertThat(gitRemoteForGradle.getHeadBranch().getOrNull()).isNull();
    assertThat(gitRemoteForGradle.getHeadBranchRefName().getOrNull()).isNull();
    assertThat(gitRemoteForGradle.headBranch()).isNull();
    assertThat(gitRemoteForGradle.headBranchRefName()).isNull();
  }
}

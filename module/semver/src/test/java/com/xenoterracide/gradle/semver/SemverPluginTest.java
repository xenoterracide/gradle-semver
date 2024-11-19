// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SemverPluginTest {

  Project project;
  Git git;

  @BeforeEach
  void setupProject() throws Exception {
    project = ProjectBuilder.builder().build();

    var msg1 = "one";
    var msg2 = "two";
    var msg3 = "three";

    git = new InitCommand().setDirectory(project.getProjectDir()).call();
    var one = git.commit().setMessage(msg1).setAllowEmpty(true).call();
    var two = git.commit().setMessage(msg2).setAllowEmpty(true).call();
    var three = git.commit().setMessage(msg3).setAllowEmpty(true).call();

    git.tag().setAnnotated(true).setMessage(msg1).setName("v0.1.1").setObjectId(one).call();
    git.tag().setAnnotated(true).setMessage(msg2).setName("v0.1.2").setObjectId(two).call();
    git.tag().setAnnotated(true).setMessage(msg3).setName("v0.1.3").setObjectId(three).call();
  }

  @Test
  void apply() {
    project.getPluginManager().apply(SemverPlugin.class);
    var semver = project.getExtensions().getByType(SemverExtension.class);
    assertThat(semver.getGitDescribed()).hasToString("0.1.3");
  }
}

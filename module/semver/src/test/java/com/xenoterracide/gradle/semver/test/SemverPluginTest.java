// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.xenoterracide.gradle.semver.SemverExtension;
import com.xenoterracide.gradle.semver.SemverPlugin;
import java.io.File;
import org.eclipse.jgit.api.Git;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

class SemverPluginTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  @NonNull
  File projectDir;

  Project project;
  Git git;

  @BeforeEach
  void setupProject() throws Exception {
    project = ProjectBuilder.builder().withProjectDir(projectDir).build();

    var msg1 = "one";
    var msg2 = "two";
    var msg3 = "three";

    git = Git.init().setDirectory(project.getProjectDir()).call();
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
    var semver = project.getExtensions().getByType(SemverExtension.class).getProvider();
    assertThat(semver.get()).hasToString("0.1.3");
  }
}

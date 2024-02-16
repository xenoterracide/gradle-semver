// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.
package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SemVerPluginTest {

  @TempDir
  File testProjectDir;

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

  @BeforeEach
  @SuppressWarnings("NullAway.Init")
  public void setupRunner() throws IOException, GitAPIException {
    Files.writeString(testProjectDir.toPath().resolve("settings.gradle"), "rootProject.name = 'hello-world'");
    Files.writeString(
      testProjectDir.toPath().resolve("build.gradle"),
      """
      plugins {
        id 'com.xenoterracide.gradle.sem-ver'
      }

      task getSemVer {
        logger.quiet("version:" + project.version)
      }
      """
    );
    try (var git = Git.init().setDirectory(testProjectDir).call()) {
      git.commit().setMessage("initial commit").call();
    }
  }

  @Test
  void apply() {
    project.getPluginManager().apply(SemVerPlugin.class);
    assertThat(project.getVersion()).isEqualTo("0.1.3");
  }

  @Test
  void snapshot() throws Exception {
    git.commit().setMessage("four").setAllowEmpty(true).call();

    project.getPluginManager().apply(SemVerPlugin.class);
    assertThat(project.getVersion().toString()).startsWith("0.1.3-1-g").endsWith("-SNAPSHOT");
  }

  @Test
  void withRunner() {
    var build = GradleRunner
      .create()
      .withDebug(true)
      .withProjectDir(testProjectDir)
      .withArguments("getSemVer")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains("version:unspecified");
  }
}

// SPDX-License-Identifier: Apache-2.0
// Copyright © 2024 Caleb Cushing.
package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SemVerPluginTest {

  @TempDir
  File testProjectDir;

  @BeforeEach
  @SuppressWarnings("NullAway.Init")
  public void setup() throws IOException, GitAPIException {
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
  void t() {
    var build = GradleRunner
      .create()
      .withProjectDir(testProjectDir)
      .withArguments("getSemVer")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains("version:unspecified");
  }
}

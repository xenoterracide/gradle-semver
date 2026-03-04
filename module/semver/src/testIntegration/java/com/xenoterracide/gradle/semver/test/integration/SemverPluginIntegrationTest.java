// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

class SemverPluginIntegrationTest {

  static final String SCRIPT = """
    plugins {
      id("com.xenoterracide.gradle.semver")
    }
    """;

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File testProjectDir;

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File noGitProjectDir;

  @BeforeEach
  public void setupRunner() throws IOException, GitAPIException {
    Files.writeString(testProjectDir.toPath().resolve("settings.gradle"), "rootProject.name = " + "'hello-world'");
    try (var git = Git.init().setDirectory(testProjectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();
    }
  }

  @Test
  @Disabled("enable for local debugging only")
  void debug() throws IOException {
    Files.writeString(testProjectDir.toPath().resolve("build.gradle"), SCRIPT);
    var build = GradleRunner.create()
      .withDebug(true)
      .withProjectDir(testProjectDir)
      .withArguments("semverVersion", "--stacktrace")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains("0.1.0", "BUILD SUCCESSFUL");
  }

  @Test
  @Disabled("enable for local debugging only")
  void noGitDirDebug() throws IOException {
    Files.writeString(noGitProjectDir.toPath().resolve("build.gradle"), SCRIPT);
    var build = GradleRunner.create()
      .withDebug(true)
      .withProjectDir(noGitProjectDir)
      .withArguments("semverVersion", "--stacktrace")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains("0.0.0-alpha.0.0", "BUILD SUCCESSFUL");
  }

  @ParameterizedTest
  @ArgumentsSource(NormalRepoArgumentsProvider.class)
  void configurationCache(String task, String expectedVersionPattern, String fileName) throws IOException {
    Files.writeString(testProjectDir.toPath().resolve(fileName), SCRIPT);
    var build = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withArguments(task, "--configuration-cache", "--stacktrace", "--quiet")
      .withPluginClasspath()
      .build();

    // Without remote HEAD, treated as topic branch - version includes metadata
    // Pattern: 0.1.0+branch.<name>.git.0.<sha> or empty string for version task
    assertThat(build.getOutput()).matches(expectedVersionPattern);
  }

  @ParameterizedTest
  @ArgumentsSource(NoGitDirArgumentsProvider.class)
  void noGitDir(String task, String expectedVersion, String fileName) throws IOException {
    Files.writeString(noGitProjectDir.toPath().resolve("settings.gradle"), "rootProject.name = " + "'hello-world'");
    Files.writeString(noGitProjectDir.toPath().resolve(fileName), SCRIPT);

    var build = GradleRunner.create()
      .withProjectDir(noGitProjectDir)
      .withArguments(task, "--configuration-cache", "--stacktrace", "--quiet")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).isEqualToIgnoringNewLines(expectedVersion);
  }

  static class NormalRepoArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
      return Stream.of(
        // semver plugin outputs - on exact tag, clean version without metadata
        Arguments.of("semverVersion", "0.1.0", "build.gradle"),
        Arguments.of("semverVersion", "0.1.0", "build.gradle.kts"),
        // project.version outputs (default is unset in the test projects, so `--quiet` yields empty)
        Arguments.of("version", "", "build.gradle"),
        Arguments.of("version", "", "build.gradle.kts")
      );
    }
  }

  static class NoGitDirArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
      return Stream.of(
        // semver plugin fallback outputs - no git returns 0.0.0
        Arguments.of("semverVersion", "0.0.0", "build.gradle"),
        Arguments.of("semverVersion", "0.0.0", "build.gradle.kts"),
        // project.version outputs (default is unset in the test projects, so `--quiet` yields empty)
        Arguments.of("version", "", "build.gradle"),
        Arguments.of("version", "", "build.gradle.kts")
      );
    }
  }
}

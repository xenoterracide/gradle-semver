// SPDX-FileCopyrightText: Copyright © 2024 - 2026 Caleb Cushing
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

  static final String LOGGING = """
        logger.quiet("semver:" + semver.provider.get())
        logger.quiet("semver:" + semver)
    """;
  static final String GROOVY_SCRIPT = """
    plugins {
      id("com.xenoterracide.gradle.semver")
    }

      task logSemver {
    %s
    }
    """;
  static final String KOTLIN_SCRIPT = """
    plugins {
      id("com.xenoterracide.gradle.semver")
    }

      tasks.register("logSemver") {
    %s
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
    Files.writeString(testProjectDir.toPath().resolve("build.gradle"), String.format(GROOVY_SCRIPT, LOGGING));
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
    Files.writeString(noGitProjectDir.toPath().resolve("build.gradle"), String.format(GROOVY_SCRIPT, LOGGING));
    var build = GradleRunner.create()
      .withDebug(true)
      .withProjectDir(noGitProjectDir)
      .withArguments("semverVersion", "--stacktrace")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains("0.0.0", "BUILD SUCCESSFUL");
  }

  @ParameterizedTest
  @ArgumentsSource(BuildScriptArgumentsProvider.class)
  void configurationCache(String task, String expectedVersion, String fileName, String buildScript) throws IOException {
    Files.writeString(testProjectDir.toPath().resolve(fileName), buildScript);
    var build = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withArguments(task, "--configuration-cache", "--stacktrace")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains(expectedVersion, "BUILD SUCCESSFUL");
  }

  @ParameterizedTest
  @ArgumentsSource(BuildScriptArgumentsProvider.class)
  void noGitDir(String task, String expectedVersion, String fileName, String buildScript) throws IOException {
    Files.writeString(noGitProjectDir.toPath().resolve("settings.gradle"), "rootProject.name = " + "'hello-world'");
    Files.writeString(noGitProjectDir.toPath().resolve(fileName), buildScript);

    var build = GradleRunner.create()
      .withProjectDir(noGitProjectDir)
      .withArguments(task, "--configuration-cache", "--stacktrace")
      .withPluginClasspath()
      .build();

    var expected = "semverVersion".equals(task) || "logSemver".equals(task) ? "0.0.0-alpha.0.0" : expectedVersion;

    assertThat(build.getOutput()).contains(expected, "BUILD SUCCESSFUL");
  }

  static class BuildScriptArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
      return Stream.of(
        // semver plugin outputs
        Arguments.of("semverVersion", "0.1.0", "build.gradle", String.format(GROOVY_SCRIPT, LOGGING)),
        Arguments.of("semverVersion", "0.1.0", "build.gradle.kts", String.format(KOTLIN_SCRIPT, LOGGING)),
        // project.version outputs (default is `unspecified` in the test projects)
        Arguments.of("version", "unspecified", "build.gradle", String.format(GROOVY_SCRIPT, LOGGING)),
        Arguments.of("version", "unspecified", "build.gradle.kts", String.format(KOTLIN_SCRIPT, LOGGING)),
        // user-defined task exercising semver extension access
        Arguments.of("logSemver", "0.1.0", "build.gradle", String.format(GROOVY_SCRIPT, LOGGING)),
        Arguments.of("logSemver", "0.1.0", "build.gradle.kts", String.format(KOTLIN_SCRIPT, LOGGING))
      );
    }
  }
}

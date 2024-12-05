// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

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

class SemverPluginIntegrationTest {

  static final String LOGGING =
    """
    logger.quiet("semver:" + semver.provider().get())
    logger.quiet("branch:" + gitMetadata.branch.getOrNull() )
    logger.quiet("commit:" + gitMetadata.commit.getOrNull())
    logger.quiet("commitShort:" + gitMetadata.uniqueShort.getOrNull())
    logger.quiet("latestTag:" + gitMetadata.tag.getOrNull())
    logger.quiet("commitDistance:" + gitMetadata.distance.get())
    logger.quiet("status:" + gitMetadata.status.get())
    """;
  static final String GROOVY_SCRIPT =
    """
    plugins {
      id("com.xenoterracide.gradle.semver")
    }

    task getSemVer {
    %s
    }
    """;
  static final String KOTLIN_SCRIPT =
    """
    plugins {
      id("com.xenoterracide.gradle.semver")
    }

    tasks.register("getSemVer") {
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
      .withArguments("getSemVer", "--stacktrace")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains("semver:0.1.0");
  }

  @Test
  @Disabled("enable for local debugging only")
  void noGitDirDebug() throws IOException {
    Files.writeString(noGitProjectDir.toPath().resolve("build.gradle"), String.format(GROOVY_SCRIPT, LOGGING));
    var build = GradleRunner.create()
      .withDebug(true)
      .withProjectDir(noGitProjectDir)
      .withArguments("getSemVer", "--stacktrace")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains("semver:0.0.0");
  }

  @ParameterizedTest
  @ArgumentsSource(BuildScriptArgumentsProvider.class)
  void configurationCache(String fileName, String buildScript) throws IOException {
    Files.writeString(testProjectDir.toPath().resolve(fileName), buildScript);
    var build = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withArguments("getSemVer", "--configuration-cache", "--stacktrace")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains("semver:0.1.0");
  }

  @ParameterizedTest
  @ArgumentsSource(BuildScriptArgumentsProvider.class)
  void noGitDir(String fileName, String buildScript) throws IOException {
    Files.writeString(noGitProjectDir.toPath().resolve("settings.gradle"), "rootProject.name = " + "'hello-world'");
    Files.writeString(noGitProjectDir.toPath().resolve(fileName), buildScript);

    var build = GradleRunner.create()
      .withProjectDir(noGitProjectDir)
      .withArguments("getSemVer", "--configuration-cache", "--stacktrace")
      .withPluginClasspath()
      .build();

    assertThat(build.getOutput()).contains("semver:0.0.0");
  }

  static class BuildScriptArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
        Arguments.of("build.gradle", String.format(GROOVY_SCRIPT, LOGGING)),
        Arguments.of("build.gradle.kts", String.format(KOTLIN_SCRIPT, LOGGING))
      );
    }
  }
}

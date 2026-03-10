// SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.test.integration;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.eclipse.jgit.api.Git;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

class PublishedSemverPluginIntegrationTest {

  private static final String VERSION = "9.9.9-bootstrap-it";

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File workingDirectory;

  @Test
  void publishedPluginCanApplyGitPlugin() throws Exception {
    var repoRoot = findRepoRoot();
    var sourceCopy = workingDirectory.toPath().resolve("source");

    copyRepo(repoRoot, sourceCopy);
    patchBootstrapBuild(sourceCopy);
    publishPlugins(sourceCopy);
    var consumerProject = workingDirectory.toPath().resolve("consumer");
    createConsumerProject(consumerProject);

    var build = GradleRunner.create()
      .withProjectDir(consumerProject.toFile())
      .withArguments("semverVersion", "--stacktrace", "--quiet")
      .build();

    assertThat(build.getOutput()).doesNotContain("NoClassDefFoundError").doesNotContain("GitPlugin").isNotBlank();
  }

  private static Path findRepoRoot() {
    return findRepoRoot(Path.of(System.getProperty("user.dir")).toAbsolutePath());
  }

  private static Path findRepoRoot(Path current) {
    if (Files.exists(current.resolve("settings.gradle.kts")) && Files.isDirectory(current.resolve("module/semver"))) {
      return current;
    }
    var parent = current.getParent();
    if (parent == null) {
      throw new IllegalStateException("Could not locate repository root from " + System.getProperty("user.dir"));
    }
    return findRepoRoot(parent);
  }

  private static void copyRepo(Path source, Path target) throws IOException {
    Files.walkFileTree(
      source,
      new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          if (skip(dir)) {
            return SKIP_SUBTREE;
          }
          Files.createDirectories(target.resolve(source.relativize(dir)));
          return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (!skip(file)) {
            Files.copy(file, target.resolve(source.relativize(file)));
          }
          return CONTINUE;
        }
      }
    );
  }

  private static boolean skip(Path path) {
    var name = path.getFileName();
    if (name == null) {
      return false;
    }
    return name.toString().equals(".git") || name.toString().equals(".gradle") || name.toString().equals("build");
  }

  private static void patchBootstrapBuild(Path sourceCopy) throws IOException {
    var buildFile = sourceCopy.resolve("build.gradle.kts");
    var buildScript = Files.readString(buildFile)
      .replace("import org.semver4j.Semver\n", "")
      .replace("  alias(libs.plugins.semver)\n", "")
      .replace(
        """
        version =
          providers
            .environmentVariable(\"IS_PUBLISHING\")
            .flatMap { semver.provider }
            .getOrElse(Semver.ZERO)
        """,
        "version = \"" + VERSION + "\"\n"
      );
    Files.writeString(buildFile, buildScript);

    Files.writeString(
      sourceCopy.resolve("buildscript-gradle.lockfile"),
      """
      # This is a Gradle generated file for dependency locking.
      # Manual edits can break the build and are not advised.
      # This file is expected to be part of source control.
      empty=
      """
    );
  }

  private static void publishPlugins(Path sourceCopy) {
    var gradleUserHome = Path.of(System.getProperty("user.home"), ".gradle").toString();
    GradleRunner.create()
      .withProjectDir(sourceCopy.toFile())
      .withArguments(
        ":git:publishToMavenLocal",
        ":semver:publishToMavenLocal",
        "-g",
        gradleUserHome,
        "-PghUsername=test",
        "-PghPassword=test",
        "--write-locks",
        "--no-configuration-cache"
      )
      .build();
  }

  private static void createConsumerProject(Path consumerProject) throws Exception {
    Files.createDirectories(consumerProject);
    Files.writeString(
      consumerProject.resolve("settings.gradle.kts"),
      """
      pluginManagement {
        repositories {
          mavenLocal()
          gradlePluginPortal()
        }
      }
      rootProject.name = "consumer"
      """
    );
    Files.writeString(
      consumerProject.resolve("build.gradle.kts"),
      """
      plugins {
        id("com.xenoterracide.gradle.semver") version "%s"
      }
      """.formatted(VERSION)
    );

    try (var git = Git.init().setDirectory(consumerProject.toFile()).call()) {
      git.add().addFilepattern(".").call();
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();
    }
  }
}

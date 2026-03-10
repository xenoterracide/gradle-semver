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
    var mavenRepo = workingDirectory.toPath().resolve("m2");

    copyRepo(repoRoot, sourceCopy);
    patchBootstrapBuild(sourceCopy);
    publishPlugins(sourceCopy, mavenRepo);
    var consumerProject = workingDirectory.toPath().resolve("consumer");
    createConsumerProject(consumerProject);

    var build = GradleRunner.create()
      .withProjectDir(consumerProject.toFile())
      .withArguments("-Dmaven.repo.local=" + mavenRepo, "semverVersion", "--stacktrace", "--quiet")
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
    var buildScript = Files.readString(buildFile).replace(
      "version = if (providers.environmentVariable(\"IS_PUBLISHING\").isPresent) " +
        "calculateProjectVersion() else \"0.0.0\"",
      "version = \"" + VERSION + "\""
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

  private static void publishPlugins(Path sourceCopy, Path mavenRepo) {
    var gradleUserHome = Path.of(System.getProperty("user.home"), ".gradle").toString();
    var ghUsername = System.getenv().getOrDefault("ORG_GRADLE_PROJECT_ghUsername", "test");
    var ghPassword = System.getenv().getOrDefault("ORG_GRADLE_PROJECT_ghPassword", "test");
    GradleRunner.create()
      .withProjectDir(sourceCopy.toFile())
      .withArguments(
        "-Dmaven.repo.local=" + mavenRepo,
        ":git:publishToMavenLocal",
        ":semver:publishToMavenLocal",
        "-g",
        gradleUserHome,
        "-PghUsername=" + ghUsername,
        "-PghPassword=" + ghPassword,
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

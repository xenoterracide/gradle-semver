// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.test;

import static com.xenoterracide.gradle.git.fixtures.CommitTools.commit;
import static org.assertj.core.api.Assertions.assertThat;

import com.xenoterracide.gradle.semver.SemverExtension;
import com.xenoterracide.gradle.semver.SemverPlugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.URIish;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests that verify version ordering with both Maven and Semver4j.
 *
 * <p>These tests build real versions from git repositories and verify they sort correctly
 * according to both Maven's ComparableVersion (which considers build metadata) and
 * Semver4j (which ignores build metadata per spec).</p>
 */
class VersionOrderingIntegrationTest {

  static final String MAIN = "main";
  static final String ORIGIN = "origin";

  final Logger log = Logging.getLogger(this.getClass());

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File bareRepo;

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File projectDir;

  private static SemverExtension getVersion(File dir) {
    var pb = ProjectBuilder.builder().withProjectDir(dir);
    var project = pb.build();
    project.getPluginManager().apply(SemverPlugin.class);
    return project.getExtensions().getByType(SemverExtension.class);
  }

  /**
   * Verifies version ordering progression:
   * 1. Exact tag (lowest)
   * 2. HEAD branch after tag
   * 3. Topic branch after tag
   *
   * <p>Maven's ComparableVersion should respect this ordering, with HEAD branch
   * sorting higher than topic branch due to build metadata differences.</p>
   */
  @Test
  void versionProgressionFromTag() throws Exception {
    Git.init().setDirectory(bareRepo).setInitialBranch(MAIN).setBare(true).call().close();

    try (var git = Git.init().setDirectory(projectDir).setInitialBranch(MAIN).call()) {
      git.remoteAdd().setUri(new URIish(bareRepo.toURI().toString())).setName(ORIGIN).call();

      // Setup remote HEAD
      commit(git);
      git.push().setRemote(ORIGIN).setPushAll().call();
      var setHead = new ProcessBuilder("git", "remote", "set-head", ORIGIN, "--auto")
        .directory(projectDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start();
      try (var reader = new BufferedReader(new InputStreamReader(setHead.getErrorStream(), StandardCharsets.UTF_8))) {
        var exitCode = setHead.waitFor();
        log.warn("set-head: {}", reader.lines().toList());
        assertThat(exitCode).as("git remote set-head should succeed").isEqualTo(0);
      }

      // Get versions at different points
      var versions = new ArrayList<VersionSnapshot>();

      // 1. On exact tag v1.0.0
      git.tag().setName("v1.0.0").call();
      versions.add(new VersionSnapshot("on tag v1.0.0", getVersion(projectDir).getProvider().get().toString()));

      // 2. 1 commit on main (HEAD branch)
      commit(git);
      git.push().setRemote(ORIGIN).setPushAll().call();
      versions.add(new VersionSnapshot("1 commit on main", getVersion(projectDir).getProvider().get().toString()));

      // 3. Create topic branch with 1 commit
      git.checkout().setCreateBranch(true).setName("feature-x").call();
      commit(git);
      versions.add(new VersionSnapshot("topic branch 1 commit", getVersion(projectDir).getProvider().get().toString()));

      // Log all versions
      versions.forEach(v -> log.warn("{}: {}", v.description(), v.version()));

      // Verify ordering with Maven ComparableVersion
      verifyMavenOrdering(versions);
    }
  }

  /**
   * Verifies version ordering between different branch states.
   * Topic branches naturally have higher prerelease numbers than HEAD branch
   * at the same point in time because they include more commits from the tag.
   */
  @Test
  void topicBranchSortsHigherThanHeadBranch() throws Exception {
    Git.init().setDirectory(bareRepo).setInitialBranch(MAIN).setBare(true).call().close();

    try (var git = Git.init().setDirectory(projectDir).setInitialBranch(MAIN).call()) {
      git.remoteAdd().setUri(new URIish(bareRepo.toURI().toString())).setName(ORIGIN).call();

      // Setup
      commit(git);
      git.push().setRemote(ORIGIN).setPushAll().call();
      var setHead = new ProcessBuilder("git", "remote", "set-head", ORIGIN, "--auto")
        .directory(projectDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start();
      var exitCode = setHead.waitFor();
      assertThat(exitCode).as("git remote set-head should succeed").isEqualTo(0);

      git.tag().setName("v1.0.0").call();

      // 2 commits on main
      commit(git);
      commit(git);
      git.push().setRemote(ORIGIN).setPushAll().call();

      // Get HEAD branch version
      var headVersion = getVersion(projectDir).getProvider().get().toString();
      log.warn("HEAD branch: {}", headVersion);

      // Create topic branch from same point, 1 commit
      git.checkout().setCreateBranch(true).setName("feature-x").call();
      commit(git);
      var topicVersion = getVersion(projectDir).getProvider().get().toString();
      log.warn("Topic branch: {}", topicVersion);

      // Topic branch sorts higher because it has more commits from tag (higher prerelease)
      assertThat(new ComparableVersion(topicVersion))
        .as("Topic branch version %s should sort higher than HEAD branch %s", topicVersion, headVersion)
        .isGreaterThan(new ComparableVersion(headVersion));
    }
  }

  private static void verifyMavenOrdering(List<VersionSnapshot> versions) {
    for (int i = 0; i < versions.size() - 1; i++) {
      var current = versions.get(i);
      var next = versions.get(i + 1);

      assertThat(new ComparableVersion(current.version()))
        .as(
          "%s (%s) should be less than %s (%s)",
          current.description(),
          current.version(),
          next.description(),
          next.version()
        )
        .isLessThan(new ComparableVersion(next.version()));
    }
  }

  private record VersionSnapshot(String description, String version) {}
}

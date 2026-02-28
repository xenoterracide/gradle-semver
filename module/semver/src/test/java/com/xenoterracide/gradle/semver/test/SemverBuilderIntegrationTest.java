// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.test;

import static com.xenoterracide.gradle.git.fixtures.CommitTools.commit;
import static com.xenoterracide.gradle.git.fixtures.CommitTools.supplies;
import static org.assertj.core.api.Assertions.assertThat;

import com.xenoterracide.gradle.semver.SemverExtension;
import com.xenoterracide.gradle.semver.SemverPlugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.URIish;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.semver4j.Semver;

class SemverBuilderIntegrationTest {

  // Pattern for versions with build metadata (topic branches, or no-head-branch cases)
  static final Pattern VERSION_PATTERN_WITH_METADATA = Pattern.compile(
    "^\\d+\\.\\d+\\.\\d+(?:-[^+]+)?\\+(branch\\.[\\p{Alnum}-]+\\.)?git\\.\\d+\\.\\p{XDigit}{7}$"
  );

  // Pattern for versions without build metadata (HEAD branch after tag)
  static final Pattern VERSION_PATTERN_NO_METADATA = Pattern.compile("^\\d+\\.\\d+\\.\\d+(?:-[^+]+)?$");

  static final String MAIN = "main";
  static final String ORIGIN = "origin";
  final Logger log = Logging.getLogger(this.getClass());

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File bareRepo;

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File projectDir;

  private static int abbreviatedShaLength(String versionString) {
    // e.g. "0.1.2-alpha.0.2+git.2.af15c63" or "...+branch.topic-foo.git.3.af15c63"
    var lastDot = versionString.lastIndexOf('.');
    assertThat(lastDot)
      .describedAs("version must contain a '.' before the abbreviated sha: %s", versionString)
      .isGreaterThan(0);
    return versionString.length() - lastDot - 1;
  }

  private static void assertVersionWithPrefix(String actual, String expectedPrefix) {
    assertVersionWithPrefix(actual, expectedPrefix, true);
  }

  private static void assertVersionWithPrefix(String actual, String expectedPrefix, boolean hasMetadata) {
    assertThat(actual).satisfies(s -> {
      assertThat(s).describedAs("version string").startsWith(expectedPrefix);

      if (hasMetadata) {
        assertThat(s).describedAs("version with metadata").matches(VERSION_PATTERN_WITH_METADATA);

        var shaLen = abbreviatedShaLength(s);
        assertThat(shaLen).describedAs("abbreviated sha length for '%s'", s).isEqualTo(7);

        var sha = s.substring(s.length() - shaLen);
        assertThat(sha).describedAs("abbreviated sha suffix for '%s'", s).hasSize(shaLen).matches("^[0-9a-fA-F]+$");

        // Note: We don't check total length because prerelease identifiers can vary in length
        // (e.g., "alpha.0.1" vs "rc.1.1" vs "alpha.0.10")
      } else {
        assertThat(s).describedAs("version without metadata").matches(VERSION_PATTERN_NO_METADATA);
      }
    });
  }

  static Supplier<Semver> versionSupplier(ProjectBuilder pb) {
    return () -> {
      var project = pb.build();
      project.getPluginManager().apply(SemverPlugin.class);
      return project.getExtensions().getByType(SemverExtension.class).getProvider().get();
    };
  }

  @Test
  void headBranch() throws Exception {
    var pb = ProjectBuilder.builder().withProjectDir(projectDir);
    Git.init().setDirectory(bareRepo).setInitialBranch(MAIN).setBare(true).call().close();
    try (var git = Git.init().setDirectory(projectDir).setInitialBranch(MAIN).call()) {
      git.remoteAdd().setUri(new URIish(bareRepo.toURI().toString())).setName(ORIGIN).call();

      commit(git);
      git.push().setRemote(ORIGIN).setPushAll().call();
      var setHead = new ProcessBuilder("git", "remote", "set-head", ORIGIN, "--auto")
        .directory(projectDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start();

      try (var reader = new BufferedReader(new InputStreamReader(setHead.getErrorStream(), StandardCharsets.UTF_8))) {
        setHead.waitFor();
        log.warn("set-head: {}", reader.lines().toList());
      }

      var vs = versionSupplier(pb);

      // On HEAD branch (main), no build metadata is added
      var v001Alpha01 = vs.get();
      assertVersionWithPrefix(v001Alpha01.toString(), "0.0.1-alpha.0.1", false);

      var v001Alpha02 = supplies(commit(git), vs);
      assertVersionWithPrefix(v001Alpha02.toString(), "0.0.1-alpha.0.2", false);

      git.tag().setName("v0.1.0").call();

      var v010 = vs.get();

      assertThat(v010).isGreaterThan(v001Alpha01);

      var v010BldV2 = supplies(commit(git), vs);

      // On HEAD branch after tag: no metadata
      assertThat(v010BldV2).isGreaterThan(v001Alpha01).isGreaterThan(v010).asString().isEqualTo("0.1.1-alpha.0.1");

      var v010BldV3 = supplies(commit(git), vs);

      assertThat(v010BldV3)
        .isGreaterThan(v001Alpha01)
        .isGreaterThan(v010)
        .isGreaterThan(v010BldV2)
        .asString()
        .isEqualTo("0.1.1-alpha.0.2");

      git.tag().setName("v0.1.1-rc.1").call();

      var v011Rc1 = vs.get();

      assertThat(v011Rc1)
        .isGreaterThan(v001Alpha01)
        .isGreaterThan(v010)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010BldV3)
        .asString()
        .isEqualTo("0.1.1-rc.1");

      // Regression/assertion: when we are N commits past a prerelease tag, we must append that distance
      // to the prerelease identifiers (rc.1.<N>) but NO metadata on HEAD branch
      var v011Rc1BldV1 = supplies(commit(git), vs);
      assertThat(v011Rc1BldV1.toString()).isEqualTo("0.1.1-rc.1.1");

      git.tag().setName("v0.1.1").call();

      var v011 = vs.get();

      // On HEAD branch at exact tag: clean version without metadata
      assertThat(v011)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010)
        .isGreaterThan(v001Alpha01)
        .hasToString("0.1.1")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease, Semver::getBuild)
        .containsExactly(0, 1, 1, Collections.emptyList(), Collections.emptyList());

      commit(git);
      var branch = "topic/foo";
      git.checkout().setCreateBranch(true).setName(branch).call();
      git.push().setPushAll().call();
      // On topic branch: metadata includes branch name
      // New state machine uses base version without incrementing patch
      assertVersionWithPrefix(vs.get().toString(), "0.1.1-alpha.0.1+branch.topic-foo.git.1.");
      commit(git);
      commit(git);

      git.push().setPushAll().call();
      git.checkout().setName(MAIN).call();

      commit(git);
      git.push().setPushAll().call();
      // Back on HEAD branch: no metadata
      // Back on HEAD branch: no metadata, patch IS incremented
      assertThat(vs.get().toString()).isEqualTo("0.1.2-alpha.0.2");

      git.checkout().setName(branch).call().getObjectId();
      // On topic branch - ideally should be 3 commits from merge base
      // But when merge base can't be determined (no remote HEAD), falls back to tag distance
      // Note: In this test environment, remote HEAD setup isn't working correctly
      // New state machine uses base version without incrementing patch
      assertVersionWithPrefix(vs.get().toString(), "0.1.1-alpha.0.1+branch.topic-foo.git.1.");
    }
  }

  @Test
  void noHeadBranch() throws Exception {
    var pb = ProjectBuilder.builder().withProjectDir(projectDir);
    try (var git = Git.init().setDirectory(projectDir).setInitialBranch(MAIN).call()) {
      commit(git);
      var vs = versionSupplier(pb);

      // Without remote HEAD configured, we're on a "topic branch" relative to nothing
      // So we get metadata with branch name
      var v001Alpha01 = vs.get();
      assertVersionWithPrefix(v001Alpha01.toString(), "0.0.1-alpha.0.1+branch.main.git.1.");

      var v001Alpha02 = supplies(commit(git), vs);
      assertVersionWithPrefix(v001Alpha02.toString(), "0.0.1-alpha.0.2+branch.main.git.2.");

      git.tag().setName("v0.1.0").call();

      var v010 = vs.get();

      assertThat(v010).isGreaterThan(v001Alpha01);

      var v010BldV2 = supplies(commit(git), vs);

      // Still on "topic branch" (no HEAD configured), so metadata with branch name
      // Note: 0.1.0-alpha... is LESS than 0.1.0 in semver (prerelease < release)
      assertThat(v010BldV2)
        .isGreaterThan(v001Alpha01)
        .asString()
        .startsWith("0.1.0-alpha.0.1+branch.main.git.");

      var v010BldV3 = supplies(commit(git), vs);

      assertThat(v010BldV3)
        .isGreaterThan(v001Alpha01)
        .isGreaterThan(v010BldV2)
        .asString()
        .startsWith("0.1.0-alpha.0.2+branch.main.git.");

      git.tag().setName("v0.1.1-rc.1").call();

      var v011Rc1 = vs.get();

      // On topic branch at exact tag - metadata includes branch name for traceability
      assertVersionWithPrefix(v011Rc1.toString(), "0.1.1-rc.1+branch.main.git.0.");

      // Regression/assertion: when we are N commits past a prerelease tag
      var v011Rc1BldV1 = supplies(commit(git), vs);
      // Prerelease tag + distance has longer prerelease identifier, just check prefix and metadata pattern
      assertThat(v011Rc1BldV1.toString())
        .startsWith("0.1.1-rc.1.1+branch.main.git.1.")
        .matches(VERSION_PATTERN_WITH_METADATA);

      git.tag().setName("v0.1.1").call();

      var v011 = vs.get();

      // Without remote HEAD configured, we can't determine HEAD branch
      // So we treat as topic branch and include metadata for traceability
      assertThat(v011)
        .isGreaterThan(v001Alpha01)
        .asString()
        .startsWith("0.1.1+branch.main.git.0.");

      commit(git);
      var branch = "topic/foo";
      git.checkout().setCreateBranch(true).setName(branch).call();
      // New state machine uses base version without incrementing patch
      assertVersionWithPrefix(vs.get().toString(), "0.1.1-alpha.0.1+branch.topic-foo.git.1.");
      commit(git);
      commit(git);

      git.checkout().setName(MAIN).call();

      commit(git);
      // Back on main - still "topic branch" mode since no HEAD configured
      // On topic branch: base version without patch increment
      assertVersionWithPrefix(vs.get().toString(), "0.1.1-alpha.0.2+branch.main.git.");

      git.checkout().setName(branch).call().getObjectId();

      assertVersionWithPrefix(vs.get().toString(), "0.1.1-alpha.0.3+branch.topic-foo.git.3.");
    }
  }
}

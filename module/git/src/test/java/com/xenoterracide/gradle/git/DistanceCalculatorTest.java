// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import static com.xenoterracide.gradle.git.fixtures.CommitTools.commit;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Splitter;
import java.io.File;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

class DistanceCalculatorTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File projectDir;

  @Test
  void distanceBetween() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      // Create initial commit
      var initialCommit = commit(git);

      // Create 3 more commits
      var commit1 = commit(git);
      commit(git);
      var commit3 = commit(git);

      var calculator = new DistanceCalculator(() -> git);

      // Distance from initial to commit3 should be 3
      assertThat(calculator.distanceBetween(initialCommit, commit3)).isEqualTo(3);

      // Distance from commit1 to commit3 should be 2
      assertThat(calculator.distanceBetween(commit1, commit3)).isEqualTo(2);

      // Distance from commit3 to itself should be 0
      assertThat(calculator.distanceBetween(commit3, commit3)).isEqualTo(0);

      // Distance in reverse direction should be 0 (log range is empty)
      assertThat(calculator.distanceBetween(commit3, initialCommit)).isEqualTo(0);
    }
  }

  @Test
  void distanceFromTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      commit(git);
      git.tag().setName("v0.1.0").call();

      commit(git);
      var commit2 = commit(git);

      var calculator = new DistanceCalculator(() -> git);

      // Distance from the tag to commit2 should be 2
      var tagRef = git.getRepository().resolve("v0.1.0");
      assertThat(tagRef).isNotNull();
      assertThat(calculator.distanceBetween(tagRef, commit2)).isEqualTo(2);
    }
  }

  @Test
  void distanceFromTagMatchesGitDescribe() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      // Create initial commit and tag
      commit(git);
      git.tag().setName("v1.0.0").call();

      // Create 5 more commits
      for (var i = 0; i < 5; i++) {
        commit(git);
      }

      // Get distance from git describe
      var describeResult = git.describe().setTags(true).setLong(true).call();
      assertThat(describeResult).isNotNull();
      // describeResult format: v1.0.0-5-g<sha>
      var describeDistance = Integer.parseInt(Splitter.on('-').splitToList(describeResult).get(1));

      // Our distance calculation should match git describe
      var calculator = new DistanceCalculator(() -> git);
      var ourDistance = calculator.apply("HEAD");

      assertThat(ourDistance).isEqualTo(describeDistance).isEqualTo(5);
    }
  }
}

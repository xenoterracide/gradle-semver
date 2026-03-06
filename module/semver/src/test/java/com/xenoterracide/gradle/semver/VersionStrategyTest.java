// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionStrategyTest {

  @Test
  void onExactTagHeadStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0")
      .distanceFromTag(0)
      .isOnTagExact(true)
      .currentBranch("main")
      .headBranch("main")
      .isHeadBranch(true)
      .distanceFromMergeBase(0)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var version = new OnExactTagHeadStrategy(ctx).calculate();
    assertThat(version.toString()).isEqualTo("1.0.0");
  }

  @Test
  void onExactTagTopicStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0")
      .distanceFromTag(0)
      .isOnTagExact(true)
      .currentBranch("feature-x")
      .headBranch("main")
      .isHeadBranch(false)
      .distanceFromMergeBase(0)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var version = new OnExactTagTopicStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("1.0.0+branch.feature-x.git.0.");
  }

  @Test
  void afterTagHeadStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0")
      .distanceFromTag(5)
      .isOnTagExact(false)
      .currentBranch("main")
      .headBranch("main")
      .isHeadBranch(true)
      .distanceFromMergeBase(5)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var version = new AfterTagHeadStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("1.0.1-alpha.0.5+git.5.");
  }

  @Test
  void afterTagTopicStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0")
      .distanceFromTag(5)
      .isOnTagExact(false)
      .currentBranch("feature-x")
      .headBranch("main")
      .isHeadBranch(false)
      .distanceFromMergeBase(2)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var version = new AfterTagTopicStrategy(ctx).calculate();
    // prerelease uses tag distance (5, like HEAD branch), metadata uses merge base distance (2)
    assertThat(version.toString()).startsWith("1.0.1-alpha.0.5+branch.feature-x.git.2.");
  }

  @Test
  void afterPrereleaseTagHeadStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0-rc.1")
      .distanceFromTag(3)
      .isOnTagExact(false)
      .currentBranch("main")
      .headBranch("main")
      .isHeadBranch(true)
      .distanceFromMergeBase(3)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var version = new AfterTagHeadStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("1.0.0-rc.1.3+git.3.");
  }

  @Test
  void afterPrereleaseTagTopicStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0-rc.1")
      .distanceFromTag(3)
      .isOnTagExact(false)
      .currentBranch("feature-x")
      .headBranch("main")
      .isHeadBranch(false)
      .distanceFromMergeBase(1)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var version = new AfterTagTopicStrategy(ctx).calculate();
    // prerelease uses tag distance (3, like HEAD branch), metadata uses merge base distance (1)
    assertThat(version.toString()).startsWith("1.0.0-rc.1.3+branch.feature-x.git.1.");
  }

  @Test
  void noTagHeadStrategy() {
    var ctx = GitContext.builder()
      .nearestTag(null)
      .distanceFromTag(5)
      .isOnTagExact(false)
      .currentBranch("main")
      .headBranch("main")
      .isHeadBranch(true)
      .distanceFromMergeBase(5)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var version = new NoTagHeadStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("0.0.1-alpha.0.5+git.5.");
  }

  @Test
  void noTagTopicStrategy() {
    var ctx = GitContext.builder()
      .nearestTag(null)
      .distanceFromTag(5)
      .isOnTagExact(false)
      .currentBranch("feature-x")
      .headBranch("main")
      .isHeadBranch(false)
      .distanceFromMergeBase(2)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var version = new NoTagTopicStrategy(ctx).calculate();
    // prerelease uses distance from tag (5, like HEAD branch), metadata uses distance from merge base (2)
    assertThat(version.toString()).startsWith("0.0.1-alpha.0.5+branch.feature-x.git.2.");
  }

  @Test
  void dirtyMarkerAppended() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0")
      .distanceFromTag(0)
      .isOnTagExact(true)
      .currentBranch("feature-x")
      .headBranch("main")
      .isHeadBranch(false)
      .distanceFromMergeBase(0)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(true)
      .isShallowClone(false)
      .build();
    var version = new OnExactTagTopicStrategy(ctx).calculate();
    assertThat(version.toString()).endsWith(".dirty");
  }

  @Test
  void afterTagTopicStrategyRequiresTag() {
    var ctx = GitContext.builder()
      .nearestTag(null)
      .distanceFromTag(5)
      .isOnTagExact(false)
      .currentBranch("feature-x")
      .headBranch("main")
      .isHeadBranch(false)
      .distanceFromMergeBase(2)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    // This should use NoTagTopicStrategy, not AfterTagTopicStrategy
    var version = new NoTagTopicStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("0.0.1");
  }

  @Test
  void strategySelectionNoTag() {
    var ctx = GitContext.builder()
      .nearestTag(null)
      .distanceFromTag(3)
      .isOnTagExact(false)
      .currentBranch("main")
      .headBranch("main")
      .isHeadBranch(true)
      .distanceFromMergeBase(3)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();

    var version = new NoTagHeadStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("0.0.1-alpha.0.3");
  }

  // Tests for VersionStrategyFactory.determineStrategy()

  @Test
  void factorySelectsOnExactTagHeadStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0")
      .distanceFromTag(0)
      .isOnTagExact(true)
      .currentBranch("main")
      .headBranch("main")
      .isHeadBranch(true)
      .distanceFromMergeBase(0)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(OnExactTagHeadStrategy.class);
    assertThat(strategy.calculate().toString()).isEqualTo("1.0.0");
  }

  @Test
  void factorySelectsOnExactTagTopicStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0")
      .distanceFromTag(0)
      .isOnTagExact(true)
      .currentBranch("feature-x")
      .headBranch("main")
      .isHeadBranch(false)
      .distanceFromMergeBase(0)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(OnExactTagTopicStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("1.0.0+branch.feature-x.git.0.");
  }

  @Test
  void factorySelectsAfterTagHeadStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0")
      .distanceFromTag(5)
      .isOnTagExact(false)
      .currentBranch("main")
      .headBranch("main")
      .isHeadBranch(true)
      .distanceFromMergeBase(5)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(AfterTagHeadStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("1.0.1-alpha.0.5+git.5.");
  }

  @Test
  void factorySelectsAfterTagTopicStrategy() {
    var ctx = GitContext.builder()
      .nearestTag("v1.0.0")
      .distanceFromTag(5)
      .isOnTagExact(false)
      .currentBranch("feature-x")
      .headBranch("main")
      .isHeadBranch(false)
      .distanceFromMergeBase(2)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(AfterTagTopicStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("1.0.1-alpha.0.5+branch.feature-x.git.2.");
  }

  @Test
  void factorySelectsNoTagHeadStrategy() {
    var ctx = GitContext.builder()
      .nearestTag(null)
      .distanceFromTag(5)
      .isOnTagExact(false)
      .currentBranch("main")
      .headBranch("main")
      .isHeadBranch(true)
      .distanceFromMergeBase(5)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(NoTagHeadStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("0.0.1-alpha.0.5+git.5.");
  }

  @Test
  void factorySelectsNoTagTopicStrategy() {
    var ctx = GitContext.builder()
      .nearestTag(null)
      .distanceFromTag(5)
      .isOnTagExact(false)
      .currentBranch("feature-x")
      .headBranch("main")
      .isHeadBranch(false)
      .distanceFromMergeBase(2)
      .shortSha("abc1234")
      .fullSha("fullsha")
      .isDirty(false)
      .isShallowClone(false)
      .build();
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(NoTagTopicStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("0.0.1-alpha.0.5+branch.feature-x.git.2.");
  }
}

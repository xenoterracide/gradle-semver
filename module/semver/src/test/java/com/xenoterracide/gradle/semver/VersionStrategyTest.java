// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionStrategyTest {

  // CHECKSTYLE.OFF: ParameterNumber - test helper method needs many parameters
  static GitContext createContext(
    String nearestTag,
    long distanceFromTag,
    boolean isOnTagExact,
    String currentBranch,
    String headBranch,
    boolean isHeadBranch,
    long distanceFromMergeBase,
    String shortSha,
    String fullSha,
    boolean isDirty,
    boolean isShallowClone
  ) {
    return GitContext.builder()
      .nearestTag(nearestTag)
      .distanceFromTag(distanceFromTag)
      .isOnTagExact(isOnTagExact)
      .currentBranch(currentBranch)
      .headBranch(headBranch)
      .isHeadBranch(isHeadBranch)
      .distanceFromMergeBase(distanceFromMergeBase)
      .shortSha(shortSha)
      .fullSha(fullSha)
      .isDirty(isDirty)
      .isShallowClone(isShallowClone)
      .build();
  }

  // CHECKSTYLE.ON: ParameterNumber

  @Test
  void onExactTagHeadStrategy() {
    var ctx = createContext("v1.0.0", 0, true, "main", "main", true, 0, "abc1234", "fullsha", false, false);
    var version = new OnExactTagHeadStrategy(ctx).calculate();
    assertThat(version.toString()).isEqualTo("1.0.0");
  }

  @Test
  void onExactTagTopicStrategy() {
    var ctx = createContext("v1.0.0", 0, true, "feature-x", "main", false, 0, "abc1234", "fullsha", false, false);
    var version = new OnExactTagTopicStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("1.0.0+branch.feature-x.git.0.");
  }

  @Test
  void afterTagHeadStrategy() {
    var ctx = createContext("v1.0.0", 5, false, "main", "main", true, 5, "abc1234", "fullsha", false, false);
    var version = new AfterTagHeadStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("1.0.1-alpha.0.5+git.5.");
  }

  @Test
  void afterTagTopicStrategy() {
    var ctx = createContext("v1.0.0", 5, false, "feature-x", "main", false, 2, "abc1234", "fullsha", false, false);
    var version = new AfterTagTopicStrategy(ctx).calculate();
    // prerelease uses tag distance (5, like HEAD branch), metadata uses merge base distance (2)
    assertThat(version.toString()).startsWith("1.0.1-alpha.0.5+branch.feature-x.git.2.");
  }

  @Test
  void afterPrereleaseTagHeadStrategy() {
    var ctx = createContext("v1.0.0-rc.1", 3, false, "main", "main", true, 3, "abc1234", "fullsha", false, false);
    var version = new AfterTagHeadStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("1.0.0-rc.1.3+git.3.");
  }

  @Test
  void afterPrereleaseTagTopicStrategy() {
    var ctx = createContext("v1.0.0-rc.1", 3, false, "feature-x", "main", false, 1, "abc1234", "fullsha", false, false);
    var version = new AfterTagTopicStrategy(ctx).calculate();
    // prerelease uses tag distance (3, like HEAD branch), metadata uses merge base distance (1)
    assertThat(version.toString()).startsWith("1.0.0-rc.1.3+branch.feature-x.git.1.");
  }

  @Test
  void noTagHeadStrategy() {
    var ctx = createContext(null, 5, false, "main", "main", true, 5, "abc1234", "fullsha", false, false);
    var version = new NoTagHeadStrategy(ctx).calculate();
    assertThat(version.toString()).startsWith("0.0.1-alpha.0.5+git.5.");
  }

  @Test
  void noTagTopicStrategy() {
    var ctx = createContext(null, 5, false, "feature-x", "main", false, 2, "abc1234", "fullsha", false, false);
    var version = new NoTagTopicStrategy(ctx).calculate();
    // prerelease uses distance from tag (5, like HEAD branch), metadata uses distance from merge base (2)
    assertThat(version.toString()).startsWith("0.0.1-alpha.0.5+branch.feature-x.git.2.");
  }

  @Test
  void dirtyMarkerAppended() {
    var ctx = createContext("v1.0.0", 0, true, "feature-x", "main", false, 0, "abc1234", "fullsha", true, false);
    var version = new OnExactTagTopicStrategy(ctx).calculate();
    assertThat(version.toString()).endsWith(".dirty");
  }

  @Test
  void afterTagTopicStrategyRequiresTag() {
    var ctx = createContext(null, 5, false, "feature-x", "main", false, 2, "abc1234", "fullsha", false, false);
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
    var ctx = createContext("v1.0.0", 0, true, "main", "main", true, 0, "abc1234", "fullsha", false, false);
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(OnExactTagHeadStrategy.class);
    assertThat(strategy.calculate().toString()).isEqualTo("1.0.0");
  }

  @Test
  void factorySelectsOnExactTagTopicStrategy() {
    var ctx = createContext("v1.0.0", 0, true, "feature-x", "main", false, 0, "abc1234", "fullsha", false, false);
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(OnExactTagTopicStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("1.0.0+branch.feature-x.git.0.");
  }

  @Test
  void factorySelectsAfterTagHeadStrategy() {
    var ctx = createContext("v1.0.0", 5, false, "main", "main", true, 5, "abc1234", "fullsha", false, false);
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(AfterTagHeadStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("1.0.1-alpha.0.5+git.5.");
  }

  @Test
  void factorySelectsAfterTagTopicStrategy() {
    var ctx = createContext("v1.0.0", 5, false, "feature-x", "main", false, 2, "abc1234", "fullsha", false, false);
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(AfterTagTopicStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("1.0.1-alpha.0.5+branch.feature-x.git.2.");
  }

  @Test
  void factorySelectsNoTagHeadStrategy() {
    var ctx = createContext(null, 5, false, "main", "main", true, 5, "abc1234", "fullsha", false, false);
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(NoTagHeadStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("0.0.1-alpha.0.5+git.5.");
  }

  @Test
  void factorySelectsNoTagTopicStrategy() {
    var ctx = createContext(null, 5, false, "feature-x", "main", false, 2, "abc1234", "fullsha", false, false);
    var strategy = VersionStrategyFactory.determineStrategy(ctx);
    assertThat(strategy).isInstanceOf(NoTagTopicStrategy.class);
    assertThat(strategy.calculate().toString()).startsWith("0.0.1-alpha.0.5+branch.feature-x.git.2.");
  }
}

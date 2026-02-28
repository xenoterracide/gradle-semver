// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.internal;

import org.semver4j.Semver;

/**
 * Factory for creating version calculation strategies based on git context.
 *
 * <p>The factory creates one of 6 strategies based on two dimensions:</p>
 * <ul>
 *   <li>Tag relationship: ON_EXACT_TAG, AFTER_TAG, or NO_TAG</li>
 *   <li>Branch type: HEAD_BRANCH or TOPIC_BRANCH</li>
 * </ul>
 */
public final class VersionStrategyFactory {

  private VersionStrategyFactory() {
    // utility class
  }

  /**
   * Determines the appropriate strategy based on git context.
   *
   * @param ctx the git context
   * @return the version strategy for this context
   */
  public static VersionStrategy determineStrategy(GitContext ctx) {
    var hasTag = ctx.hasTagInHistory();
    var onExactTag = ctx.isOnTagExact();
    var isHeadBranch = ctx.isHeadBranch();

    VersionStrategy strategy;
    if (hasTag) {
      if (onExactTag) {
        strategy = isHeadBranch ? new OnExactTagHeadStrategy() : new OnExactTagTopicStrategy();
      } else {
        strategy = isHeadBranch ? new AfterTagHeadStrategy() : new AfterTagTopicStrategy();
      }
    } else {
      strategy = isHeadBranch ? new NoTagHeadStrategy() : new NoTagTopicStrategy();
    }
    return strategy;
  }

  /**
   * Calculates the semantic version using the appropriate strategy.
   *
   * @param ctx the git context
   * @return the calculated semantic version
   */
  public static Semver calculate(GitContext ctx) {
    var strategy = determineStrategy(ctx);
    return strategy.calculate(ctx);
  }
}

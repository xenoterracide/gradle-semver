// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.internal;

import org.semver4j.Semver;

/**
 * State machine for determining the correct version calculation state
 * based on git context.
 *
 * <p>The state machine has 6 states based on two dimensions:</p>
 * <ul>
 *   <li>Tag relationship: ON_EXACT_TAG, AFTER_TAG, or NO_TAG</li>
 *   <li>Branch type: HEAD_BRANCH or TOPIC_BRANCH</li>
 * </ul>
 */
public final class VersionStateMachine {

  private VersionStateMachine() {
    // utility class
  }

  /**
   * Determines the appropriate state based on git context.
   *
   * @param ctx the git context
   * @return the version state for this context
   */
  public static VersionState determineState(GitContext ctx) {
    boolean hasTag = ctx.hasTagInHistory();
    boolean onExactTag = ctx.isOnTagExact();
    boolean isHeadBranch = ctx.isHeadBranch();

    VersionState state;
    if (hasTag) {
      if (onExactTag) {
        state = isHeadBranch ? new OnExactTagHeadBranch() : new OnExactTagTopicBranch();
      } else {
        state = isHeadBranch ? new AfterTagHeadBranch() : new AfterTagTopicBranch();
      }
    } else {
      state = isHeadBranch ? new NoTagHeadBranch() : new NoTagTopicBranch();
    }
    return state;
  }

  /**
   * Calculates the semantic version using the state machine.
   *
   * @param ctx the git context
   * @return the calculated semantic version
   */
  public static Semver calculate(GitContext ctx) {
    VersionState state = determineState(ctx);
    return state.calculate(ctx);
  }
}

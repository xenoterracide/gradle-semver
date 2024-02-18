// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import java.util.Objects;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.InvalidPatternException;

class PorcelainGit {

  private static final String VERSION_PREFIX = "v";
  private static final String VERSION_GLOB = VERSION_PREFIX + "[0-9]*.[0-9]*.[0-9]*";

  private final Git git;

  PorcelainGit(@NonNull Git git) {
    this.git = Objects.requireNonNull(git);
  }

  String describe() {
    try {
      return git.describe().setMatch(VERSION_GLOB).call();
    } catch (GitAPIException | InvalidPatternException e) {
      throw new RuntimeException(e);
    }
  }
}

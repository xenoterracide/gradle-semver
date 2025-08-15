// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.jspecify.annotations.Nullable;

class MergeBaseFinder {

  private final Repository repo;

  MergeBaseFinder(Repository repo) {
    this.repo = repo;
  }

  public Optional<ObjectId> find(@Nullable GitRemote gitRemote) {
    if (gitRemote == null || gitRemote.headBranch() == null) return Optional.empty();
    try {
      var current = Optional.ofNullable(this.repo.findRef(Constants.HEAD)).map(Ref::getObjectId).orElseThrow();
      var remote = Optional.ofNullable(this.repo.findRef(gitRemote.headBranchRefName()))
        .map(Ref::getObjectId)
        .orElseThrow();

      try (var walk = new RevWalk(this.repo)) {
        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(List.of(walk.parseCommit(remote), walk.parseCommit(current)));

        var mergeBase = walk.next();

        return Optional.ofNullable(mergeBase).map(ObjectId::toObjectId);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}

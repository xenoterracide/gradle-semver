// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MergeBaseFinder {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Repository repo;

  MergeBaseFinder(Repository repo) {
    this.repo = repo;
  }

  public Optional<ObjectId> find(@Nullable GitRemote gitRemote) {
    if (gitRemote == null || gitRemote.headBranch() == null) return Optional.empty();
    try {
      var current = Optional.ofNullable(this.repo.findRef(Constants.HEAD)).map(Ref::getObjectId).orElseThrow();
      var remote = Optional.ofNullable(this.repo.findRef(gitRemote.headBranch())).map(Ref::getObjectId).orElseThrow();

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

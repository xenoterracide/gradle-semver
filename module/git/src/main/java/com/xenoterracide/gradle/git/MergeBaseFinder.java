// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
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
    var headBranchRef = extractHeadBranchRef(gitRemote);
    if (headBranchRef == null) {
      return Optional.empty();
    }
    try {
      return this.findMergeBase(headBranchRef);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static @Nullable String extractHeadBranchRef(@Nullable GitRemote gitRemote) {
    if (gitRemote == null || gitRemote.headBranch() == null) {
      return null;
    }
    return gitRemote.headBranchRefName();
  }

  private Optional<ObjectId> findMergeBase(String headBranchRef) throws IOException {
    var currentOpt = this.resolveRef(Constants.HEAD);
    var remoteOpt = this.resolveRef(headBranchRef);

    if (currentOpt.isEmpty() || remoteOpt.isEmpty()) {
      return Optional.empty();
    }

    return this.calculateMergeBase(currentOpt.get(), remoteOpt.get());
  }

  private Optional<ObjectId> resolveRef(String refName) throws IOException {
    return Optional.ofNullable(this.repo.findRef(refName)).map(Ref::getObjectId);
  }

  private Optional<ObjectId> calculateMergeBase(ObjectId current, ObjectId remote)
    throws IOException {
    try (var walk = new RevWalk(this.repo)) {
      walk.setRevFilter(RevFilter.MERGE_BASE);
      walk.markStart(List.of(walk.parseCommit(remote), walk.parseCommit(current)));
      var mergeBase = walk.next();
      return Optional.ofNullable(mergeBase).map(ObjectId::toObjectId);
    }
  }
}

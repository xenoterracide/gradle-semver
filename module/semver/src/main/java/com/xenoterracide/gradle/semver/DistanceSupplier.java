// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.google.errorprone.annotations.Var;
import io.vavr.control.Try;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DistanceSupplier implements Supplier<Optional<Long>> {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Repository repo;
  private final @Nullable GitRemote gitRemote;

  DistanceSupplier(Repository repo, @Nullable GitRemote gitRemote) {
    this.repo = repo;
    this.gitRemote = gitRemote;
  }

  Optional<Long> getDistance(RevCommit mergeBase) {
    this.log.warn("merge base: {} {}", mergeBase, mergeBase.getShortMessage());

    try (var walk = new RevWalk(this.repo)) {
      walk.setRevFilter(RevFilter.ALL);
      walk.sort(RevSort.COMMIT_TIME_DESC, true);

      var tags =
        this.repo.getRefDatabase()
          .getRefsByPrefix("tags/v")
          .stream()
          .map(Ref::getObjectId)
          .filter(Objects::nonNull)
          .flatMap(oid -> Try.of(() -> walk.parseCommit(oid)).toJavaStream())
          .collect(Collectors.toList());

      if (tags.contains(mergeBase)) return Optional.of(0L);

      @Var
      var distance = 1L;
      for (var next : walk) {
        this.log.warn("{} {}", next, next.getShortMessage());
        distance += 1;
      }
      return Optional.of(distance);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Optional<Long> get() {
    if (this.gitRemote == null || this.gitRemote.headBranch() == null) return Optional.empty();
    try {
      var current = Optional.ofNullable(this.repo.findRef(Constants.HEAD)).map(Ref::getObjectId).orElseThrow();
      var remote = Optional.ofNullable(this.repo.findRef(this.gitRemote.headBranch()))
        .map(Ref::getObjectId)
        .orElseThrow();

      try (var walk = new RevWalk(this.repo)) {
        walk.sort(RevSort.COMMIT_TIME_DESC, true);
        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(List.of(walk.parseCommit(remote), walk.parseCommit(current)));
        walk.next();

        var mergeBase = walk.next();

        return Optional.ofNullable(mergeBase).flatMap(this::getDistance);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}

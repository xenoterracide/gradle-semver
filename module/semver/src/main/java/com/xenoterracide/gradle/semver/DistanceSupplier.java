// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import io.vavr.control.Try;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.jspecify.annotations.Nullable;

class DistanceSupplier implements Supplier<Optional<Long>> {

  private final Repository repo;
  private final @Nullable GitRemote gitRemote;

  DistanceSupplier(Repository repo, @Nullable GitRemote gitRemote) {
    this.repo = repo;
    this.gitRemote = gitRemote;
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
        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(List.of(walk.parseCommit(remote), walk.parseCommit(current)));

        var tagStream =
          this.repo.getRefDatabase()
            .getRefsByPrefix("tags/v")
            .stream()
            .map(Ref::getObjectId)
            .filter(Objects::nonNull)
            .flatMap(oid -> Try.of(() -> walk.parseCommit(oid)).toJavaStream())
            .collect(Collectors.toList());

        return Optional.of(
          StreamSupport.stream(walk.spliterator(), false).takeWhile(Predicate.not(tagStream::contains)).count()
        );
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}

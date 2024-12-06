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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.jspecify.annotations.Nullable;

class DistanceSupplier implements Function<@Nullable GitRemote, Optional<Long>> {

  private final Repository repo;

  DistanceSupplier(Repository repo) {
    this.repo = repo;
  }

  @Override
  public Optional<Long> apply(@Nullable GitRemote gitRemote) {
    if (gitRemote == null || gitRemote.headBranch() == null) return Optional.empty();
    try {
      var remote = Objects.requireNonNull(this.repo.findRef(gitRemote.getName() + "/" + gitRemote.headBranch()));
      var current = Objects.requireNonNull(this.repo.findRef(Constants.HEAD));

      try (var walk = new RevWalk(this.repo)) {
        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(List.of(walk.parseCommit(remote.getObjectId()), walk.parseCommit(current.getObjectId())));

        var tagStream =
          this.repo.getRefDatabase()
            .getRefsByPrefix("tags/v")
            .stream()
            .flatMap(ref -> Try.of(() -> walk.parseCommit(ref.getObjectId())).toJavaStream())
            .collect(Collectors.toList());

        @Var
        var distance = 0L;
        for (var next : walk) {
          if (tagStream.contains(next)) {
            break;
          } else {
            distance += 1;
          }
        }
        return Optional.of(distance);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}

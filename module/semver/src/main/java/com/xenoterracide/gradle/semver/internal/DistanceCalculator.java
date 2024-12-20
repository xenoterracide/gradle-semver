// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistanceCalculator implements Function<String, Long> {

  private final Supplier<TryGit> git;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public DistanceCalculator(Supplier<TryGit> git) {
    this.git = git;
  }

  long distanceFromNoTag(ObjectId oid) {
    return this.git.get()
      .tryCommand(g -> g.log().add(oid).all())
      .map(IterableTools::of)
      .map(Stream::count)
      .recover(NoSuchElementException.class, 0L)
      .recover(GitMetadataImpl.allWith(0L))
      .onFailure(e -> this.log.error("failed to get distance without a tag", e))
      .get();
  }

  public long distance(ObjectId oid) {
    return this.git.get()
      .tryGit(Describer.describe(oid))
      .filter(Objects::nonNull)
      .map(Describer.Described::distance)
      .recover(NoSuchElementException.class, e -> this.distanceFromNoTag(oid))
      .onFailure(e -> this.log.error("failed to get distance", e))
      .getOrElse(0L);
  }

  @Override
  public Long apply(String revString) {
    var rev = this.git.get().tryGit(git -> git.getRepository().resolve(revString)).getOrElse(() -> null);
    if (rev == null) return 0L;
    return this.distance(rev);
  }
}

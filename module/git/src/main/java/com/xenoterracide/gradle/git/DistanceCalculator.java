// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DistanceCalculator implements Function<String, Long> {

  private final TryGit git;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  DistanceCalculator(TryGit git) {
    this.git = git;
  }

  long distanceFromNoTag(ObjectId oid) {
    return this.git.tryCommand(g -> g.log().add(oid).all())
      .map(IterableTools::of)
      .map(Stream::count)
      .recover(NoSuchElementException.class, 0L)
      .recover(GitMetadataImpl.allWith(0L))
      .onFailure(e -> this.log.error("failed to get distance without a tag", e))
      .get();
  }

  long distance(ObjectId oid) {
    return this.git.tryGit(Describer.describe(oid))
      .map(Describer.Described::distance)
      .recover(NoSuchElementException.class, e -> this.distanceFromNoTag(oid))
      .onFailure(e -> this.log.error("failed to get distance", e))
      .getOrElse(0L);
  }

  @Override
  public Long apply(String revString) {
    var rev = this.git.tryGit(git -> git.getRepository().resolve(revString)).getOrElse(() -> null);
    if (rev == null) return 0L;
    return this.distance(rev);
  }
}

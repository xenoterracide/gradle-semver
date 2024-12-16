// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistanceCalculator {

  private final Try<Git> git;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public DistanceCalculator(Try<Git> git) {
    this.git = git;
  }

  <R> Try<R> tryCommand(CheckedFunction1<Git, R> command) {
    return this.git.mapTry(command).onFailure(e -> this.log.debug("failed", e)).filter(Objects::nonNull);
  }

  long distanceFromNoTag(ObjectId oid) {
    return this.tryCommand(g -> g.log().add(oid).all().call())
      .map(IterableTools::of)
      .map(Stream::count)
      .recover(NoSuchElementException.class, 0L)
      .recover(GitMetadataImpl.allWith(0L))
      .onFailure(e -> this.log.error("failed to get distance without a tag", e))
      .get();
  }

  public long distance(ObjectId oid) {
    return this.tryCommand(Describer.describe(oid))
      .filter(Objects::nonNull)
      .map(Describer.Described::distance)
      .recover(NoSuchElementException.class, e -> this.distanceFromNoTag(oid))
      .onFailure(e -> this.log.error("failed to get distance", e))
      .getOrElse(0L);
  }
}

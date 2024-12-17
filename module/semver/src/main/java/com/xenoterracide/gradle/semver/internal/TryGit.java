// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.function.Supplier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface TryGit extends Supplier<Git> {
  default <R> Try<R> tryCommand(CheckedFunction1<Git, GitCommand<R>> command) {
    return this.tryGit(git -> command.apply(git).call());
  }

  default <R> Try<R> tryGit(CheckedFunction1<Git, R> command) {
    return Try.of(this::get)
      .mapTry(command)
      .onFailure(e -> LoggerFactory.getLogger(this.getClass()).debug("failed", e))
      .filter(Objects::nonNull);
  }
}

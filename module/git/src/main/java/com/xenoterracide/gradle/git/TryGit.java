// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.function.Supplier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

/**
 * Use this to wrap Git commands using {@link Try} to handle exceptions.
 */
@FunctionalInterface
public interface TryGit extends Supplier<@Nullable Git> {
  /**
   * Wraps a {@link GitCommand} in a {@link Try} to handle exceptions.
   *
   * @param command
   *   command to run
   * @param <R>
   *   return type
   * @return the result of the function
   */
  default <R> Try<R> tryCommand(CheckedFunction1<Git, GitCommand<@Nullable R>> command) {
    return this.tryGit(git -> command.apply(git).call());
  }

  /**
   * Wraps a Git command in a {@link Try} to handle exceptions.
   *
   * @param command
   *   to run
   * @param <R>
   *   result of the function
   * @return result of the function with filtered nulls
   */
  default <R> Try<R> tryGit(CheckedFunction1<Git, @Nullable R> command) {
    return Try.of(this::get)
      .filter(Objects::nonNull)
      .mapTry(command)
      .onFailure(e -> LoggerFactory.getLogger(this.getClass()).debug("failed", e))
      .filter(Objects::nonNull);
  }
}

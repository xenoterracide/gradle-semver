// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

public final class CommitTools {

  public static final IntSupplier NEXT_INT = IntStream.iterate(0, i -> i + 1).iterator()::nextInt;

  private CommitTools() {}

  public static ObjectId commit(Git git) throws GitAPIException {
    var commitFormat = "commit %d";
    var message = String.format(commitFormat, NEXT_INT.getAsInt());
    var commit = git.commit().setMessage(message).call();
    return commit.toObjectId();
  }

  /**
   * just for silly single statement one-liners that reduce boilerplate.
   */
  public static <T> T supplies(@Nullable ObjectId ignored, Supplier<T> supplier) {
    return supplier.get();
  }
}

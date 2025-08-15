// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git.fixtures;

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
    var message = "commit %d".formatted(NEXT_INT.getAsInt());
    var commit = git.commit().setMessage(message).call();
    return commit.toObjectId();
  }

  public static ObjectId commit(Supplier<Git> git) throws GitAPIException {
    return commit(git.get());
  }

  /**
   * just for silly single statement one-liners that reduce boilerplate.
   */
  public static <T> T supplies(@Nullable ObjectId ignored, Supplier<T> supplier) {
    return supplier.get();
  }
}

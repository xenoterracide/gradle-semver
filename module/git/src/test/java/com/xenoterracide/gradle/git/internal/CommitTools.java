// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.internal;

import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jspecify.annotations.Nullable;

public final class CommitTools {

  public static final IntSupplier NEXT_INT = IntStream.iterate(0, i -> i + 1).iterator()::nextInt;

  private CommitTools() {}

  static Void commit(Git git) throws GitAPIException {
    var commitFormat = "commit %d";
    var message = String.format(commitFormat, NEXT_INT.getAsInt());
    git.commit().setMessage(message).call();
    return null;
  }

  /**
   * just for silly single statement one-liners that reduce boilerplate.
   */
  static <T> T supplies(@Nullable Void ignored, Supplier<T> supplier) {
    return supplier.get();
  }
}

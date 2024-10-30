// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.internal;

import io.vavr.control.Try;

class NoGitDirException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  NoGitDirException() {
    super("No git directory found");
  }

  static <T> Try<T> failure() {
    return Try.failure(new NoGitDirException());
  }
}

// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import io.vavr.control.Try;

public class NoRefsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  NoRefsException() {
    super("No refs found");
  }

  static <T> Try<T> failure() {
    return Try.failure(new NoRefsException());
  }
}

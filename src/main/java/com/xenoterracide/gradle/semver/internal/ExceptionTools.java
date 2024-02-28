// SPDX-License-Identifier: Apache-2.0
// © Copyright 2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver.internal;

public final class ExceptionTools {

  private ExceptionTools() {}

  public static RuntimeException rethrow(Throwable e) {
    throw new RuntimeException(e);
  }
}

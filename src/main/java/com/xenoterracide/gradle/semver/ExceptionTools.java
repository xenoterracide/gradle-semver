// SPDX-License-Identifier: Apache-2.0
// Copyright © 2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

final class ExceptionTools {

  private ExceptionTools() {}

  static RuntimeException rethrow(Throwable e) {
    throw new RuntimeException(e);
  }
}

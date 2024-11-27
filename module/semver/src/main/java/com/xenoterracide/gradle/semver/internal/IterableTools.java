// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class IterableTools {

  private IterableTools() {}

  static <T> Stream<T> of(Iterable<T> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false);
  }
}

// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class IterableTools {

  private IterableTools() {}

  static <T> Stream<T> of(Iterable<T> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false);
  }
}

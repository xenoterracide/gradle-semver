// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.cache;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MvCacheTest {

  @Test
  void cache() {
    try (var cache = MvCache.create(Path.of("cache"))) {}
  }
}

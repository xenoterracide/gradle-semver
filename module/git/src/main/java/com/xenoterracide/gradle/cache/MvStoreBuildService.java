// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.cache;

import java.util.Map;
import java.util.function.Supplier;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.h2.mvstore.MVStore;

public abstract class MvStoreBuildService
  implements BuildService<BuildServiceParameters>, Supplier<Map<String, ?>>, AutoCloseable {

  private final MVStore store = new MVStore.Builder().open();

  @Override
  public void close() throws Exception {
    this.store.close();
  }

  @Override
  public Map<String, ?> get() {
    return this.store.openMap("cache");
  }
}

// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.cache;

import java.nio.file.Path;
import java.util.function.Supplier;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.jspecify.annotations.Nullable;

public abstract class CacheService
  implements BuildService<CacheService.Parameters>, AutoCloseable, Supplier<MvCache<?, ?>> {

  private @Nullable MvCache<?, ?> cache;

  @Override
  public MvCache<?, ?> get() {
    if (this.cache == null) {
      this.cache = MvCache.create(this.getParameters().getCacheDir().get());
    }
    return this.cache;
  }

  @Override
  public void close() {
    if (this.cache != null) {
      this.cache.close();
    }
  }

  public interface Parameters extends BuildServiceParameters {
    Property<Path> getCacheDir();
  }
}

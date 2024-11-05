// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Function;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.h2.mvstore.MVStore;
import org.jspecify.annotations.Nullable;

public abstract class MvStoreBuildService<T>
  implements BuildService<MvStoreBuildService.Parameters>, Function<Callable<T>, Provider<T>>, AutoCloseable {

  private final ProviderFactory providerFactory;
  private @Nullable MVStore store;

  protected MvStoreBuildService(ProviderFactory providerFactory) {
    this.providerFactory = providerFactory;
  }

  static MVStore createStore(Path cacheDir) {
    try {
      if (!Files.exists(cacheDir)) Files.createDirectories(cacheDir);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new MVStore.Builder().fileName(cacheDir.resolve("cache.mv.db").toString()).open();
  }

  @Override
  public void close() {
    if (this.store != null) {
      this.store.close();
    }
  }

  @Override
  public Provider<T> apply(Callable<T> tCallable) {
    if (this.store == null) this.store = createStore(this.getParameters().getCacheDir().get());

    var storeMap = this.store.openMap("cache");

    var cache = Caffeine.newBuilder().build();
    cache
      .asMap()
      .compute("cache", (k, v) -> {
        var value = storeMap.get(k);
        return value;
      });
    return providerFactory.provider(tCallable);
  }

  public interface Parameters extends BuildServiceParameters {
    Property<Path> getCacheDir();
  }
}

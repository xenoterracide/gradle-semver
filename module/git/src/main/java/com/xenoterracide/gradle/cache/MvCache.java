// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.cache;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.tuple.Pair;
import org.h2.mvstore.MVStore;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MvCache<K extends Serializable, V extends Serializable> implements AutoCloseable {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Map<K, V> memoized = new HashMap<>();
  private final MVStore store;
  private @Nullable Map<K, Pair<Instant, V>> storeMap;

  public MvCache(MVStore store) {
    this.store = store;
  }

  static <K extends Serializable, V extends Serializable> MvCache<K, V> create(Path cacheDir) {
    return new MvCache<>(createStore(cacheDir));
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
    if (!this.store.isClosed()) {
      this.store.close();
    }
  }

  public Callable<V> cache(Callable<V> call, K key) {
    return this.cache(call, key, Duration.ofDays(1));
  }

  public Callable<@Nullable V> cache(Callable<@Nullable V> call, K key, Duration cacheFor) {
    if (this.storeMap == null) this.storeMap = this.store.openMap("cache");
    return () -> this.memoized.computeIfAbsent(key, k -> this.compute(key, call, cacheFor));
  }

  @Nullable
  V compute(K key, Callable<V> call, Duration cacheFor) {
    var storeMap = Objects.requireNonNull(this.storeMap, "storeMap must not be null");

    var pair = storeMap.computeIfAbsent(key, k -> {
      try {
        return Pair.of(Instant.now().plus(cacheFor), call.call());
      } catch (Exception e) {
        this.log.error("problem computing cacheable", e);
        return null;
      }
    });

    return pair != null ? pair.getValue() : null;
  }
}

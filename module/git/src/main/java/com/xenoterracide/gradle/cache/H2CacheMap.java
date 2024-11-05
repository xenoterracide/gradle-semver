// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class H2CacheMap<K, V> implements Map<K, V> {

  private final Cache<K, V> cache = Caffeine.newBuilder().build();

  @Override
  public int size() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean containsKey(Object key) {
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    return false;
  }

  @Override
  public @Nullable V get(Object key) {
    return null;
  }

  @Override
  public @Nullable V put(K key, V value) {
    return null;
  }

  @Override
  public @Nullable V remove(Object key) {
    return null;
  }

  @Override
  public void putAll(@NotNull Map<? extends K, ? extends V> m) {}

  @Override
  public void clear() {}

  @Override
  public @NotNull Set<K> keySet() {
    return Set.of();
  }

  @Override
  public @NotNull Collection<V> values() {
    return List.of();
  }

  @Override
  public @NotNull Set<Entry<K, V>> entrySet() {
    return Set.of();
  }

  static class RemovalListener {}
}

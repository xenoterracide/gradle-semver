// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.internal;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

public class GradleTools {

  public static <T> Property<T> finalizeOnRead(Property<T> property) {
    property.finalizeValueOnRead();
    return property;
  }

  public static <T> ListProperty<T> finalizeOnRead(ListProperty<T> property) {
    property.finalizeValueOnRead();
    return property;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> MapProperty<K, V> finalizeOnRead(MapProperty<? super K, ? super V> property) {
    property.finalizeValueOnRead();
    return (MapProperty<K, V>) property;
  }
}

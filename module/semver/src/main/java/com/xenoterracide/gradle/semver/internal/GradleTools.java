// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import org.gradle.api.provider.Property;

public final class GradleTools {

  private GradleTools() {}

  public static <T> Property<T> finalOnRead(Property<T> property) {
    property.finalizeValueOnRead();
    return property;
  }
}

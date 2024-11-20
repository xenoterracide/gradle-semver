// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import org.gradle.api.provider.Property;

/**
 * Tools for making gradle less annoying to work with.
 */
public final class GradleTools {

  private GradleTools() {}

  /**
   * Finalize a property on read.
   *
   * @param property
   *   property to finalize
   * @param <T>
   *   generic on property
   * @return the input property
   */
  public static <T> Property<T> finalOnRead(Property<T> property) {
    property.finalizeValueOnRead();
    return property;
  }
}

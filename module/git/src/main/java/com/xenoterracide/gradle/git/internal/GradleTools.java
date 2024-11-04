// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.internal;

import org.gradle.api.provider.Property;

public class GradleTools {

  public static <T> Property<T> finalizeOnRead(Property<T> property) {
    property.finalizeValueOnRead();
    return property;
  }
}

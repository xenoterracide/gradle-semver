// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.provider.Property;

class GradleTools {

  static <T> Property<T> finalizeOnRead(Property<T> property) {
    property.finalizeValueOnRead();
    return property;
  }
}

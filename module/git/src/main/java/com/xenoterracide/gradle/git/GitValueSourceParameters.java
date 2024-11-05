// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import java.io.File;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSourceParameters;

public interface GitValueSourceParameters extends ValueSourceParameters {
  Property<File> getProjectDir();
}

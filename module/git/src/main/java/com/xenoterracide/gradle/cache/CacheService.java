// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.cache;

import java.nio.file.Path;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

public abstract class CacheService implements BuildService<CacheService.Parameters>, AutoCloseable {

  public interface Parameters extends BuildServiceParameters {
    Property<Path> getCacheDir();

    Property<Boolean> firstLevelOnly();
  }
}

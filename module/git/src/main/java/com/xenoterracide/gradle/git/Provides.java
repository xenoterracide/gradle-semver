// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.provider.Provider;

/**
 * A provider of a value.
 *
 * @param <T>
 *   the type of value
 */
public interface Provides<T> {
  /**
   * Returns a provider of the value.
   *
   * @return a provider of the value
   */
  Provider<T> getProvider();
}

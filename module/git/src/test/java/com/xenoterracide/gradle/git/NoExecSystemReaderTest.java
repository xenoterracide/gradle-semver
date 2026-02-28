// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NoExecSystemReaderTest {

  @Test
  void getEnvPathReturnsEmptyString() {
    var reader = new NoExecSystemReader();
    assertThat(reader.getenv("PATH")).isEmpty();
  }

  @Test
  void getEnvOtherVariableReturnsValue() {
    var reader = new NoExecSystemReader();
    // HOME should exist in most environments
    assertThat(reader.getenv("HOME")).isNotNull();
  }
}

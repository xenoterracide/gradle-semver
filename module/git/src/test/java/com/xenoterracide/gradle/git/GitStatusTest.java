// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GitStatusTest {

  @Test
  void toStringReturnsCamelCase() {
    assertThat(GitStatus.CLEAN.toString()).isEqualTo("clean");
    assertThat(GitStatus.DIRTY.toString()).isEqualTo("dirty");
    assertThat(GitStatus.NO_REPO.toString()).isEqualTo("noRepo");
  }
}

// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class ProvidedFactoryTest {

  @Test
  void propertyBoolean() {
    var project = ProjectBuilder.builder().build();
    var factory = new ProvidedFactory(project);

    var prop = factory.propertyBoolean();
    assertThat(prop).isNotNull();
    prop.set(true);
    assertThat(prop.get()).isTrue();
  }

  @Test
  void propertyString() {
    var project = ProjectBuilder.builder().build();
    var factory = new ProvidedFactory(project);

    var prop = factory.propertyString();
    assertThat(prop).isNotNull();
    prop.set("test");
    assertThat(prop.get()).isEqualTo("test");
  }

  @Test
  void property() {
    var project = ProjectBuilder.builder().build();
    var factory = new ProvidedFactory(project);

    var prop = factory.property(Long.class);
    assertThat(prop).isNotNull();
    prop.set(42L);
    assertThat(prop.get()).isEqualTo(42L);
  }
}

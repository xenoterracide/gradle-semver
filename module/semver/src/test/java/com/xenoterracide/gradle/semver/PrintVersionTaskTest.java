// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class PrintVersionTaskTest {

  // CHECKSTYLE.OFF: Deprecation - ProjectBuilder requires deprecated API
  @SuppressWarnings("deprecation")
  @Test
  void printVersionOutputsVersionText() {
    var project = ProjectBuilder.builder().build();
    var task = project.getTasks().create("printVersion", PrintVersionTask.class);
    task.getVersionText().set("1.0.0-test");

    var outputStream = new ByteArrayOutputStream();
    var originalOut = System.out;
    System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));

    try {
      task.printVersion();
    } finally {
      System.setOut(originalOut);
    }

    assertThat(outputStream.toString(StandardCharsets.UTF_8)).isEqualTo("1.0.0-test");
  }

  @SuppressWarnings("deprecation")
  @Test
  void printVersionWithEmptyTextOutputsNothing() {
    var project = ProjectBuilder.builder().build();
    var task = project.getTasks().create("printVersion", PrintVersionTask.class);

    var outputStream = new ByteArrayOutputStream();
    var originalOut = System.out;
    System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));

    try {
      task.printVersion();
    } finally {
      System.setOut(originalOut);
    }

    assertThat(outputStream.toString(StandardCharsets.UTF_8)).isEmpty();
  }
}

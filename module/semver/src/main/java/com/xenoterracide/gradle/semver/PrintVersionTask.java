// SPDX-FileCopyrightText: Copyright © 2024 - 2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/**
 * Prints a version string.
 *
 * <p>Task is designed to be configuration-cache friendly by only reading declared inputs at execution
 * time.</p>
 */
public abstract class PrintVersionTask extends DefaultTask {

  /** Creates a new task instance. */
  @Inject
  public PrintVersionTask() {}

  /**
   * The version text to print.
   *
   * @return version text
   */
  @Input
  public abstract Property<String> getVersionText();

  /** Prints {@link #getVersionText()} to standard out. */
  @TaskAction
  public void printVersion() {
    var text = this.getVersionText().getOrElse("");
    System.out.println(text);
  }
}

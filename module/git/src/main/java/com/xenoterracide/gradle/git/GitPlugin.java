// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GitPlugin implements Plugin<Project> {

  @Override
  public void apply(Project target) {
    target.getExtensions().add("git", new GitMetadataExtensionImpl(target));
  }
}

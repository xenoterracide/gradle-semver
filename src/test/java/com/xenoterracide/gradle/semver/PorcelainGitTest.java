// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PorcelainGitTest {

  @TempDir
  @NonNull
  File projectDir;

  @Test
  void gitVersion() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();

      var version = new PorcelainGit(git).describe();

      assertThat(version).matches("v[0-9]+\\.[0-9]+\\.[0-9].*");
    }
  }

  @Test
  void gitVersionNull() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();

      var version = new PorcelainGit(git).describe();

      assertThat(version).isNull();
    }
  }

  @Test
  void gitVersionNextVersion() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();
      git.commit().setMessage("second commit").call();

      var version = new PorcelainGit(git).describe();

      assertThat(version).matches("v0\\.1\\.0-1-g.*");
    }
  }

  @Test
  void gitNoCommit() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> new PorcelainGit(git).describe())
        .withCauseInstanceOf(RefNotFoundException.class);
    }
  }
}

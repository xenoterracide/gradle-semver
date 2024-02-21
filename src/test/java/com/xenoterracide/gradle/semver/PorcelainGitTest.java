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
  void getLastTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();

      var pg = new PorcelainGit(git);

      assertThat(pg.getLastTag()).isNull();

      git.tag().setName("v0.1.0").call();

      assertThat(pg.getLastTag()).matches("v0.1.0");

      git.tag().setName("v0.1.1").call();

      assertThat(pg.getLastTag()).matches("v0.1.1");
    }
  }

  @Test
  void gitVersionNull() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();

      var version = new PorcelainGit(git).getVersion();

      assertThat(version).isNull();
    }
  }

  @Test
  void gitVersionNextVersion() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();
      git.commit().setMessage("second commit").call();

      var version = new PorcelainGit(git).getVersion();

      assertThat(version).matches("v0\\.1\\.0-1-g.*");
    }
  }

  @Test
  void gitMismatchTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("latest").call();
      git.commit().setMessage("second commit").call();

      var version = new PorcelainGit(git).getVersion();

      assertThat(version).isNull();
    }
  }

  @Test
  void gitNoCommit() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> new PorcelainGit(git).getVersion())
        .withCauseInstanceOf(RefNotFoundException.class);
    }
  }
}

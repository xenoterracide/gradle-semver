// SPDX-License-Identifier: Apache-2.0
// © Copyright 2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PorcelainGitExtensionTest {

  @TempDir
  @NonNull
  File projectDir;

  @Test
  void getBranchName() {}

  @Test
  void getObjectIdFor() {}

  @Test
  void getSha() {}

  @Test
  void getHeadSha() {}

  @Test
  void getHeadShortSha() {}

  @Test
  void getLastTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();

      var pg = new PorcelainGitExtension(() -> git);
      assertThat(pg.getLastTag()).isNull();

      git.tag().setName("v0.1.0").call();
      assertThat(pg.getLastTag()).matches("v0.1.0");

      git.commit().setMessage("second commit").call();
      git.tag().setName("v0.1.1").call();
      assertThat(pg.getLastTag()).matches("v0.1.1");
    }
  }

  @Test
  void getDescribe() {}

  @Test
  void getCommitDistance() {}

  @Test
  void isDirty() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();

      var pg = new PorcelainGitExtension(() -> git);

      assertThat(pg.isDirty()).isFalse();

      Files.createFile(projectDir.toPath().resolve("test.txt"));

      assertThat(pg.isDirty()).isTrue();
    }
  }
}

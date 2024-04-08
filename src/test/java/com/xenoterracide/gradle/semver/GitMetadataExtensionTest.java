// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import java.io.File;
import java.nio.file.Files;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitMetadataExtensionTest {

  @TempDir
  @NonNull
  File projectDir;

  Try.WithResources1<Git> withResources = Try.withResources(() -> Git.init().setDirectory(projectDir).call());

  CheckedFunction1<Git, Git> setup = git -> {
    git.commit().setMessage("initial commit").call();
    git.branchCreate().setName("topic/test").call();
    git.checkout().setName("topic/test").call();
    return git;
  };

  @Test
  void getBranchName() {
    assertThat(withResources.of(setup).isFailure()).isFalse();
    var pg = new GitMetadataExtension(withResources);
    assertThat(pg.getBranch()).isEqualTo("topic/test");
  }

  @Test
  void getObjectIdFor() {
    assertThat(withResources.of(setup).isFailure()).isFalse();
    var pg = new GitMetadataExtension(withResources);
    var main = pg.getObjectIdFor("topic/test").get().getName();
    var head = pg.getObjectIdFor("HEAD").get().getName();
    assertThat(main).hasSize(40);
    assertThat(head).hasSize(40);
    assertThat(main).isEqualTo(head);
  }

  @Test
  void getSha() {
    assertThat(withResources.of(setup).isFailure()).isFalse();

    var pg = new GitMetadataExtension(withResources);
    var main = pg.getRev("topic/test");
    var head = pg.getRev("HEAD");
    assertThat(main).hasSize(40);
    assertThat(head).hasSize(40);
    assertThat(main).isEqualTo(head);
  }

  @Test
  void getHeadSha() {
    assertThat(withResources.of(setup).isFailure()).isFalse();

    var pg = new GitMetadataExtension(withResources);
    var main = pg.getRev("topic/test");
    var head = pg.getCommit();
    assertThat(main).hasSize(40);
    assertThat(head).hasSize(40);
    assertThat(main).isEqualTo(head);
  }

  @Test
  void getHeadShortSha() {
    assertThat(withResources.of(setup).isFailure()).isFalse();

    var pg = new GitMetadataExtension(withResources);
    var main = pg.getRev("topic/test");
    var head = pg.getCommitShort();
    assertThat(main).isNotNull();
    assertThat(main).hasSize(40);
    assertThat(head).hasSize(7);
    assertThat(main.substring(0, 7)).isEqualTo(head);
  }

  @Test
  void getLastTag() throws Exception {
    assertThat(withResources.of(setup).isFailure()).isFalse();

    var pg = new GitMetadataExtension(withResources);
    assertThat(pg.getLatestTag()).isNull();

    assertThat(withResources.of(git -> git.tag().setName("v0.1.0").call()).isFailure()).isFalse();
    assertThat(pg.getLatestTag()).matches("v0.1.0");

    assertThat(
      withResources
        .of(git -> {
          git.commit().setMessage("second commit").call();
          git.tag().setName("v0.1.1").call();
          return null;
        })
        .isFailure()
    ).isFalse();
    assertThat(pg.getLatestTag()).matches("v0.1.1");
  }

  @Test
  void getDescribe() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();

      var pg = new GitMetadataExtension(withResources);
      git.tag().setName("v0.1.0").call();
      assertThat(pg.getDescribe()).isEqualTo("v0.1.0");

      git.commit().setMessage("second commit").call();
      assertThat(pg.getDescribe()).matches("v0\\.1\\.0-1-g[0-9a-f]{7}");

      git.tag().setName("v0.1.1").call();
      assertThat(pg.getLatestTag()).isEqualTo("v0.1.1");
    }
  }

  @Test
  void getCommitDistance() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();

      var pg = new GitMetadataExtension(withResources);
      assertThat(pg.getCommitDistance()).isEqualTo(0);

      git.commit().setMessage("second commit").call();
      assertThat(pg.getCommitDistance()).isEqualTo(1);

      git.commit().setMessage("third commit").call();
      assertThat(pg.getCommitDistance()).isEqualTo(2);

      git.tag().setName("v0.1.1").call();
      assertThat(pg.getCommitDistance()).isEqualTo(0);
    }
  }

  @Test
  void getStatus() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();

      var pg = new GitMetadataExtension(withResources);

      assertThat(pg.getStatus()).isEqualTo(GitStatus.CLEAN);

      Files.createFile(projectDir.toPath().resolve("test.txt"));

      assertThat(pg.getStatus()).isEqualTo(GitStatus.DIRTY);
    }
  }

  @Test
  void logging() {
    var wRsrc = Try.withResources(() -> Git.open(Files.createTempDirectory("gradle-semver").toFile()));
    var pg = new GitMetadataExtension(wRsrc);
    assertThat(assertDoesNotThrow(() -> pg.getBranch())).isNull();
  }
}

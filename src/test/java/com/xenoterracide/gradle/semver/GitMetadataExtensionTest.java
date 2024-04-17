// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitMetadataExtensionTest {

  @TempDir
  @NonNull
  File projectDir;

  @Test
  void getBranchName() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.branchCreate().setName("topic/test").call();
      git.checkout().setName("topic/test").call();

      var pg = new GitMetadataExtension(() -> Optional.of(git));
      assertThat(pg.getBranch()).isEqualTo("topic/test");
    }
  }

  @Test
  void noGit() {
    var pg = new GitMetadataExtension(() -> Optional.empty());
    assertThat(pg.distance()).isSameAs(0);
    assertThat(pg.getBranch()).isNull();
    assertThat(pg.getCommit()).isNull();
    assertThat(pg.getDescribe()).isNull();
    assertThat(pg.tag()).isNull();
    assertThat(pg.getCommitShort()).isNull();
    assertThat(pg.status()).isSameAs(GitStatus.NO_REPO);
  }

  @Test
  void getObjectIdFor() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.branchCreate().setName("topic/test").call();
      git.checkout().setName("topic/test").call();

      var pg = new GitMetadataExtension(() -> Optional.of(git));
      var main = pg.getObjectIdFor("topic/test").get().getName();
      var head = pg.getObjectIdFor("HEAD").get().getName();
      assertThat(main).hasSize(40);
      assertThat(head).hasSize(40);
      assertThat(main).isEqualTo(head);
    }
  }

  @Test
  void getSha() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.branchCreate().setName("topic/test").call();
      git.checkout().setName("topic/test").call();

      var pg = new GitMetadataExtension(() -> Optional.of(git));
      var main = pg.getRev("topic/test");
      var head = pg.getRev("HEAD");
      assertThat(main).hasSize(40);
      assertThat(head).hasSize(40);
      assertThat(main).isEqualTo(head);
    }
  }

  @Test
  void getHeadSha() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.branchCreate().setName("topic/test").call();
      git.checkout().setName("topic/test").call();

      var pg = new GitMetadataExtension(() -> Optional.of(git));
      var main = pg.getRev("topic/test");
      var head = pg.getCommit();
      assertThat(main).hasSize(40);
      assertThat(head).hasSize(40);
      assertThat(main).isEqualTo(head);
    }
  }

  @Test
  void getHeadShortSha() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.branchCreate().setName("topic/test").call();
      git.checkout().setName("topic/test").call();

      var pg = new GitMetadataExtension(() -> Optional.of(git));
      var main = pg.getRev("topic/test");
      var head = pg.getCommitShort();
      assertThat(main).isNotNull();
      assertThat(main).hasSize(40);
      assertThat(head).hasSize(7);
      assertThat(main.substring(0, 7)).isEqualTo(head);
    }
  }

  @Test
  void getLastTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();

      var pg = new GitMetadataExtension(() -> Optional.of(git));
      assertThat(pg.tag()).isNull();

      git.tag().setName("v0.1.0").call();
      assertThat(pg.tag()).matches("v0.1.0");

      git.commit().setMessage("second commit").call();
      git.tag().setName("v0.1.1").call();
      assertThat(pg.tag()).matches("v0.1.1");
    }
  }

  @Test
  void getDescribe() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();

      var pg = new GitMetadataExtension(() -> Optional.of(git));
      git.tag().setName("v0.1.0").call();
      assertThat(pg.getDescribe()).isEqualTo("v0.1.0");

      git.commit().setMessage("second commit").call();
      assertThat(pg.getDescribe()).matches("v0\\.1\\.0-1-g[0-9a-f]{7}");

      git.tag().setName("v0.1.1").call();
      assertThat(pg.tag()).isEqualTo("v0.1.1");
    }
  }

  @Test
  void getCommitDistance() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      var pg = new GitMetadataExtension(() -> Optional.of(git));
      assertThat(pg.distance()).isEqualTo(0);

      git.commit().setMessage("first commit").call();

      assertThat(pg.distance()).isEqualTo(1);

      git.commit().setMessage("second commit").call();

      assertThat(pg.distance()).isEqualTo(2);

      git.tag().setName("v0.1.0").call();

      assertThat(pg.distance()).isEqualTo(0);

      git.commit().setMessage("one commit").call();
      assertThat(pg.distance()).isEqualTo(1);

      git.commit().setMessage("two commit").call();
      assertThat(pg.distance()).isEqualTo(2);

      git.tag().setName("v0.1.1").call();
      assertThat(pg.distance()).isEqualTo(0);
    }
  }

  @Test
  void getStatus() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();

      var pg = new GitMetadataExtension(() -> Optional.of(git));

      assertThat(pg.status()).isEqualTo(GitStatus.CLEAN);

      Files.createFile(projectDir.toPath().resolve("test.txt"));

      assertThat(pg.status()).isEqualTo(GitStatus.DIRTY);
    }
  }
}

// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PorcelainGitExtensionTest {

  @TempDir
  @NonNull
  File projectDir;

  @Test
  void getBranchName() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.branchCreate().setName("topic/test").call();
      git.checkout().setName("topic/test").call();

      var pg = new PorcelainGitExtension(() -> git);
      assertThat(pg.getBranchName()).isEqualTo("topic/test");
    }
  }

  @Test
  void getObjectIdFor() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.branchCreate().setName("topic/test").call();
      git.checkout().setName("topic/test").call();

      var pg = new PorcelainGitExtension(() -> git);
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

      var pg = new PorcelainGitExtension(() -> git);
      var main = pg.getSha("topic/test");
      var head = pg.getSha("HEAD");
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

      var pg = new PorcelainGitExtension(() -> git);
      var main = pg.getSha("topic/test");
      var head = pg.getHeadSha();
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

      var pg = new PorcelainGitExtension(() -> git);
      var main = pg.getSha("topic/test");
      var head = pg.getHeadShortSha();
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
  void getDescribe() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();

      var pg = new PorcelainGitExtension(() -> git);
      git.tag().setName("v0.1.0").call();
      assertThat(pg.getDescribe()).isEqualTo("v0.1.0");

      git.commit().setMessage("second commit").call();
      assertThat(pg.getDescribe()).matches("v0\\.1\\.0-1-g[0-9a-f]{7}");

      git.tag().setName("v0.1.1").call();
      assertThat(pg.getLastTag()).isEqualTo("v0.1.1");
    }
  }

  @Test
  void getCommitDistance() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();

      var pg = new PorcelainGitExtension(() -> git);
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

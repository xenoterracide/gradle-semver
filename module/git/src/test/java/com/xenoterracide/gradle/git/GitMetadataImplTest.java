// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import static com.xenoterracide.gradle.git.fixtures.CommitTools.commit;
import static com.xenoterracide.gradle.git.fixtures.CommitTools.supplies;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.function.Supplier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

class GitMetadataImplTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File projectDir;

  @Test
  void getBranchName() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.branchCreate().setName("topic/test").call();
      git.checkout().setName("topic/test").call();

      var pg = new GitMetadataImpl(() -> git);
      assertThat(pg.branch()).isEqualTo("topic/test");
    }
  }

  /*
  @Test
  void noGit() {
    var pg = new GitMetadataImpl(
      () ->
        () ->
          (Git) Try.of(() -> {
            throw new RepositoryNotFoundException(projectDir);
          }).get()
    );
    assertThat(pg.distance()).isSameAs(0L);
    assertThat(pg.branch()).isNull();
    assertThat(pg.commit()).isNull();
    assertThat(pg.tag()).isNull();
    assertThat(pg.status()).isSameAs(GitStatus.NO_REPO);
  }

   */

  @Test
  void getObjectIdFor() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.branchCreate().setName("topic/test").call();
      git.checkout().setName("topic/test").call();

      var pg = new GitMetadataImpl(() -> git);
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

      var pg = new GitMetadataImpl(() -> git);
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

      var pg = new GitMetadataImpl(() -> git);
      var main = pg.getRev("topic/test");
      var head = pg.commit();
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

      var pg = new GitMetadataImpl(() -> git);
      var main = pg.getRev("topic/test");
      var head = pg.uniqueShort();
      assertThat(main).isNotNull();
      assertThat(main).hasSize(40);
      assertThat(head).hasSize(7);
      assertThat(main.substring(0, 7)).isEqualTo(head);
    }
  }

  @Test
  void getLastTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      var pg = new GitMetadataImpl(() -> git);
      assertThat(pg.tag()).isNull();

      git.commit().setMessage("first commit").call();

      assertThat(pg.tag()).isNull();

      git.commit().setMessage("second commit").call();

      assertThat(pg.tag()).isNull();

      git.tag().setName("v0.1.0").call();
      assertThat(pg.tag()).isEqualTo("v0.1.0");

      git.commit().setMessage("second commit").call();

      assertThat(pg.tag()).isEqualTo("v0.1.0");

      git.tag().setName("v0.1.1").call();
      assertThat(pg.tag()).isEqualTo("v0.1.1");
    }
  }

  @Test
  void getCommitDistance() throws GitAPIException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      var pg = new GitMetadataImpl(() -> git);
      Supplier<Long> distance = pg::distance;
      assertThat(distance.get()).isEqualTo(0L);

      assertThat(supplies(commit(git), distance)).isEqualTo(1L);

      assertThat(supplies(commit(git), distance)).isEqualTo(2L);

      git.tag().setName("v0.1.0").call();

      assertThat(distance.get()).isEqualTo(0L);

      assertThat(supplies(commit(git), distance)).isEqualTo(1L);

      assertThat(supplies(commit(git), distance)).isEqualTo(2L);

      git.tag().setName("v0.1.1").call();
      assertThat(distance.get()).isEqualTo(0L);

      assertThat(supplies(commit(git), distance)).isEqualTo(1L);

      git.tag().setName("v0.1.2-beta.0").call();

      assertThat(distance.get()).isEqualTo(0L);
      assertThat(supplies(commit(git), distance)).isEqualTo(1L);
      assertThat(supplies(commit(git), distance)).isEqualTo(2L);
    }
  }

  @Test
  void getStatus() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();

      var pg = new GitMetadataImpl(() -> git);

      assertThat(pg.status()).isEqualTo(GitStatus.CLEAN);

      Files.createFile(projectDir.toPath().resolve("test.txt"));

      assertThat(pg.status()).isEqualTo(GitStatus.DIRTY);
    }
  }

  @Test
  void getRemotesEmpty() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      var pg = new GitMetadataImpl(() -> git);
      assertThat(pg.remotes()).isEmpty();
    }
  }
}

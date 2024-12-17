// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import static com.xenoterracide.gradle.semver.internal.CommitTools.commit;
import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Joiner;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import java.io.File;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import org.assertj.core.api.Condition;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.URIish;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MergeBaseFinderTest {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  @NonNull
  File projectDir;

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  @NonNull
  File otherDev;

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  @NonNull
  File bareRemote;

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  @NonNull
  File notSetup;

  @NonNull
  TryGit git;

  @NonNull
  ObjectId initialCommit;

  static <T> Condition<T> equalTo(@Nullable Object o) {
    return new Condition<>(isEqual(o), "equal to " + o);
  }

  @BeforeEach
  void setupRemote() {
    var uri = this.bareRemote.toURI().toASCIIString();
    Try.withResources(() -> Git.init().setDirectory(bareRemote).setBare(true).call()).of(g -> g).get();
    Try.withResources(() -> Git.init().setDirectory(otherDev).call())
      .of(g -> g)
      .andThenTry(g -> {
        g.remoteAdd().setName("origin").setUri(new URIish(uri)).call();
        this.initialCommit = g.commit().setMessage("initial commit").call();
        g.push().call();
      })
      .get();
    this.git = Try.withResources(() -> Git.cloneRepository().setDirectory(projectDir).setURI(uri).call()).of(g -> {
      var p = new ProcessBuilder()
        .directory(projectDir)
        .command("git", "remote", "set-head", "--auto", "origin")
        .start();
      try (var input = p.inputReader()) {
        log.warn("set-head: {}", input.lines().toList());
      }
      p.waitFor();
      var remoteRef = Constants.R_REMOTES + "origin" + "/" + Constants.HEAD;
      var file = g.getRepository().getDirectory().toPath().resolve(remoteRef).toFile();
      log.warn("remoteRef: '{}' exists: {}", file, file.exists());
      return g;
    })::get;
  }

  @Test
  void noRemotes() {
    var gitLocal = Try.withResources(() -> Git.init().setDirectory(notSetup).call())
      .of(g -> g)
      .andThenTry(g -> g.commit().setMessage("initial commit").call());

    var gitMetadata = new GitMetadataImpl(() -> gitLocal::get);
    var distance = new MergeBaseFinder(gitLocal.get().getRepository());

    assertThat(gitMetadata.remotes()).isEmpty();
    assertThat(distance.find(null)).isNotPresent();
  }

  @Test
  void originNoHeadBranch() throws GitAPIException, URISyntaxException {
    var uri = this.bareRemote.toURI().toASCIIString();
    try (var git = Git.init().setDirectory(notSetup).call()) {
      git.remoteAdd().setName("origin").setUri(new URIish(uri)).call();
      git.commit().setMessage("initial commit").call();
      git.push().call();

      var gitMetadata = new GitMetadataImpl(() -> () -> git);
      var mergeBase = new MergeBaseFinder(git.getRepository());

      assertThat(gitMetadata.remotes()).isNotEmpty();
      assertThat(mergeBase.find(gitMetadata.remotes().getFirst())).isNotPresent();
    }
  }

  @Test
  void originHeadBranchAllPushed() throws Throwable {
    var gitMetadata = new GitMetadataImpl(() -> this.git);
    var origin = gitMetadata.remotes().getFirst();
    var mergeBase = new MergeBaseFinder(git.get().getRepository());
    CheckedFunction0<ObjectId> remoteHead = () -> git.get().getRepository().resolve("refs/remotes/origin/HEAD");
    Supplier<String> gitLog = () -> Joiner.on('\n').join(git.tryCommand(g -> g.log().all()));

    assertThat(mergeBase.find(origin)).isPresent().hasValue(initialCommit);
    var oid1 = commit(git);
    assertThat(mergeBase.find(origin)).isPresent().hasValue(initialCommit);

    git.tryCommand(Git::push);

    log.warn("first {} | {}", oid1, remoteHead.apply());
    assertThat(mergeBase.find(origin)).isPresent().hasValue(oid1);

    git.tryCommand(g -> g.tag().setName("v0.1.0"));

    git.tryCommand(g -> g.push().setPushTags());
    assertThat(mergeBase.find(origin)).isPresent().hasValue(remoteHead.apply());

    var oid2 = commit(git);
    assertThat(mergeBase.find(origin)).isPresent().hasValue(remoteHead.apply()).isNot(equalTo(oid2));

    git.tryCommand(Git::push);

    assertThat(mergeBase.find(origin)).isPresent().as(gitLog).hasValue(oid2);
  }
}

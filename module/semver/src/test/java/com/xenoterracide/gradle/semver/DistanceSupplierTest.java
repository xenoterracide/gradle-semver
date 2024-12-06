// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import com.xenoterracide.gradle.semver.internal.GitMetadataImpl;
import io.vavr.control.Try;
import java.io.File;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.URIish;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DistanceSupplierTest {

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
  Try<Git> git;

  @BeforeEach
  void setupRemote() {
    var uri = this.bareRemote.toURI().toASCIIString();
    Try.withResources(() -> Git.init().setDirectory(bareRemote).setBare(true).call()).of(g -> g).get();
    Try.withResources(() -> Git.init().setDirectory(otherDev).call())
      .of(g -> g)
      .andThenTry(g -> {
        g.remoteAdd().setName("origin").setUri(new URIish(uri)).call();
        g.commit().setMessage("initial commit").call();
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
    });
  }

  @Test
  void noRemotes() {
    var gitLocal = Try.withResources(() -> Git.init().setDirectory(notSetup).call())
      .of(g -> g)
      .andThenTry(g -> g.commit().setMessage("initial commit").call());

    var gitMetadata = new GitMetadataImpl(() -> gitLocal);
    var distance = new DistanceSupplier(gitLocal.get().getRepository());

    assertThat(gitMetadata.remotes()).isEmpty();
    assertThat(distance.apply(null)).isNotPresent();
  }

  @Test
  void originNoHeadBranch() {
    var uri = this.bareRemote.toURI().toASCIIString();
    var gitLocal = Try.withResources(() -> Git.init().setDirectory(notSetup).call())
      .of(g -> g)
      .andThenTry(g -> {
        g.remoteAdd().setName("origin").setUri(new URIish(uri)).call();
        g.commit().setMessage("initial commit").call();
        g.push().call();
      });

    var gitMetadata = new GitMetadataImpl(() -> gitLocal);
    var distance = new DistanceSupplier(gitLocal.get().getRepository());

    assertThat(gitMetadata.remotes()).isNotEmpty();
    assertThat(distance.apply(gitMetadata.remotes().getFirst())).isNotPresent();
  }

  @Test
  void originHeadBranch() {
    var gitMetadata = new GitMetadataImpl(() -> this.git);
    var distance = new DistanceSupplier(this.git.get().getRepository());
    assertThat(distance.apply(gitMetadata.remotes().getFirst())).isPresent();
  }
}

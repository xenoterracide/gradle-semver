// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;

class GitToolsTest {

  @TempDir
  File projectDir;

  @RepeatedTest(20)
  void octal() throws GitAPIException, IOException {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      Files.createTempFile(projectDir.toPath(), "test", ".txt");
      git.add().addFilepattern(".").call();
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();
      var octals = new HashSet<String>();
      for (var i = 0; i < 1000; i++) {
        var oid = git.commit().setMessage("commit " + i).call().toObjectId();
        octals.add(GitTools.toOctal(oid, 4));
      }

      assertThat(octals).hasSize(1000);

      var oid = git.getRepository().resolve(Constants.HEAD).toObjectId();
      assertThat(GitTools.toOctal(oid, 3)).hasSize(9);
    }
  }
}

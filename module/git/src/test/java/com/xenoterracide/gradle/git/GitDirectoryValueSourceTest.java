// SPDX-FileCopyrightText: Copyright © 2025 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.git;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

class GitDirectoryValueSourceTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File rootRepo;

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File moduleA;

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File moduleB;

  @Test
  void git() throws GitAPIException, IOException {
    gitInit(rootRepo);
    var aRepo = gitInit(moduleA);
    var bRepo = gitInit(moduleB);

    try (var git = Git.open(rootRepo)) {
      try (var repo = git.submoduleAdd().setPath("moduleA").setURI(aRepo.toUri().toString()).call()) {
        assertThat(repo.getRefDatabase().getRefs()).isNotEmpty();
      }
      try (var repo = git.submoduleAdd().setPath("moduleB").setURI(bRepo.toUri().toString()).call()) {
        assertThat(repo.getRefDatabase().getRefs()).isNotEmpty();
      }
      git.commit().setMessage("two").setAllowEmpty(true).call();
    }

    var project = ProjectBuilder.builder().withProjectDir(moduleA).build();

    var dir = project
      .getProviders()
      .of(GitDirectoryValueSource.class, vs -> {
        var submodule = rootRepo.toPath().resolve("moduleA");
        vs.parameters(a -> a.getProjectDirectory().fileValue(submodule.toFile()));
      })
      .get();

    var gitDir = dir.getAsFile().toPath();
    assertThat(gitDir).isDirectory().isNotEmptyDirectory();

    assertThat(gitDir.resolve("config")).isRegularFile();
  }

  static Path gitInit(File path) throws GitAPIException {
    try (var git = Git.init().setDirectory(path).call()) {
      git.commit().setMessage("one").setAllowEmpty(true).call();
      return git.getRepository().getDirectory().toPath();
    }
  }
}

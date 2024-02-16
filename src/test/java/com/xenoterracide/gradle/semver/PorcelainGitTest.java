// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.
package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;

class PorcelainGitTest {

  @Test
  void gitVersion() throws Exception {
    var repo = new FileRepositoryBuilder().findGitDir().build();

    var git = new Git(repo);
    var version = new PorcelainGit(git).describe();

    assertThat(version).matches("v[0-9]+\\.[0-9]+\\.[0-9].*");
  }
}

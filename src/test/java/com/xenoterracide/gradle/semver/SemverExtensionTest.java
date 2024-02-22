// SPDX-License-Identifier: Apache-2.0
// © Copyright 2018-2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semver4j.Semver;

class SemverExtensionTest {

  @TempDir
  @NonNull
  File projectDir;

  @Test
  void getLastTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();

      var pg = new VersionDetailsImpl(git);
      assertThat(pg.getLastTag()).isNull();

      git.tag().setName("v0.1.0").call();
      assertThat(pg.getLastTag()).matches("v0.1.0");

      git.commit().setMessage("second commit").call();
      git.tag().setName("v0.1.1").call();
      assertThat(pg.getLastTag()).matches("v0.1.1");
    }
  }

  @Test
  void getVersion() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      var pg = new VersionDetailsImpl(git);

      var v000 = pg.getSemver();
      assertThat(v000).extracting(Semver::getVersion).isEqualTo("0.0.0-SNAPSHOT");
      assertThat(v000)
        .hasToString("0.0.0-SNAPSHOT")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease)
        .containsExactly(0, 0, 0, List.of("SNAPSHOT"));

      git.tag().setName("v0.1.0").call();
      git.commit().setMessage("second commit").call();

      var v010 = pg.getSemver();

      assertThat(v010)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o).isInstanceOf(String.class);
          if (o instanceof String s) {
            assertThat(s).matches("^0\\.1\\.0-SNAPSHOT-1-g\\p{XDigit}{7}$");
          }
        });

      git.tag().setName("v0.1.1").call();

      var v011 = pg.getSemver();

      assertThat(v011)
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease)
        .containsExactly(0, 1, 1, Collections.emptyList());

      assertThat(v011).hasToString("0.1.1");
    }
  }

  @Test
  void getVersionMismatchTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("latest").call();
      git.commit().setMessage("second commit").call();

      var pg = new VersionDetailsImpl(git);

      assertThat(pg.getSemver().getVersion()).isEqualTo("0.0.0-SNAPSHOT");

      git.tag().setName("v0.1.0").call();

      assertThat(pg.getSemver().getVersion()).isEqualTo("0.1.0");
    }
  }

  @Test
  void isCleanTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("v0.1.0").call();

      var pg = new VersionDetailsImpl(git);

      assertThat(pg.getIsCleanTag()).isTrue();

      Files.createFile(projectDir.toPath().resolve("test.txt"));

      assertThat(pg.getIsCleanTag()).isFalse();
    }
  }
}

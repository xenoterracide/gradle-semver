// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.InstanceOfAssertFactories;
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
  void getGradlePlugin() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      var pg = new SemverExtension(() -> git);

      var v000 = pg.getGradlePlugin();
      assertThat(v000).extracting(Semver::getVersion).isEqualTo("0.0.0");
      assertThat(v000)
        .hasToString("0.0.0")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease)
        .containsExactly(0, 0, 0, List.of());

      git.tag().setName("v0.1.0").call();
      git.commit().setMessage("second commit").call();

      var v010 = pg.getGradlePlugin();

      assertThat(v010)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o).asInstanceOf(InstanceOfAssertFactories.STRING).matches("^0\\.1\\.0-1-g\\p{XDigit}{7}$");
        });

      git.tag().setName("v0.1.1").call();

      var v011 = pg.getGradlePlugin();

      assertThat(v011)
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease)
        .containsExactly(0, 1, 1, Collections.emptyList());

      assertThat(v011).hasToString("0.1.1");
    }
  }

  @Test
  void getMaven() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      var pg = new SemverExtension(() -> git);

      var v000 = pg.getMaven();
      assertThat(v000).extracting(Semver::getVersion).isEqualTo("0.0.0-SNAPSHOT");
      assertThat(v000)
        .hasToString("0.0.0-SNAPSHOT")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease)
        .containsExactly(0, 0, 0, List.of("SNAPSHOT"));

      git.tag().setName("v0.1.0").call();
      git.commit().setMessage("second commit").call();

      var v010 = pg.getMaven();

      assertThat(v010)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o)
            .asInstanceOf(InstanceOfAssertFactories.STRING)
            .matches("^0\\.1\\.0-SNAPSHOT-1-g\\p{XDigit}{7}$");
        });

      git.tag().setName("v0.1.1").call();

      var v011 = pg.getMaven();

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

      var pg = new SemverExtension(() -> git);

      assertThat(pg.getMaven()).hasToString("0.0.0-SNAPSHOT");

      git.tag().setName("v0.1.0").call();

      assertThat(pg.getMaven()).hasToString("0.1.0");
    }
  }

  @Test
  void classTest() {
    assertThat(SemverExtension.class).isPublic().hasPublicMethods("getMaven", "getGradlePlugin");
  }
}

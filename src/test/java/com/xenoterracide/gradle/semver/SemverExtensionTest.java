// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semver4j.Semver;

class SemverExtensionTest {

  @TempDir
  File projectDir;

  @Test
  void getGradlePlugin() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      var pg = new SemverExtension(() -> Optional.of(git));

      var v000 = pg.getGradlePlugin();
      assertThat(v000).extracting(Semver::getVersion).isEqualTo("0.0.0");
      assertThat(v000)
        .hasToString("0.0.0")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease)
        .containsExactly(0, 0, 0, List.of());

      git.tag().setName("v0.1.0").call();

      var v010 = pg.getGradlePlugin();
      assertThat(v010).isGreaterThan(v000);

      git.commit().setMessage("second commit").call();

      var v010BldV = pg.getGradlePlugin();

      assertThat(v010BldV)
        .isGreaterThan(v000)
        .isGreaterThan(v010)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o)
            .asInstanceOf(InstanceOfAssertFactories.STRING)
            .startsWith("0.1.1-alpha.1+1.g")
            .matches("^0\\.1\\.1-alpha\\.1\\+\\d+\\.g\\p{XDigit}{7}$");
        });

      var sv = new Semver("0.1.1-alpha.1+2.g3aae11e");

      assertThat(sv).isEqualByComparingTo(v010BldV);

      git.tag().setName("v0.1.1").call();

      var v011 = pg.getGradlePlugin();

      assertThat(v011)
        .isGreaterThan(v010BldV)
        .isGreaterThan(v010)
        .isGreaterThan(v000)
        .hasToString("0.1.1")
        .extracting(
          Semver::getMajor,
          Semver::getMinor,
          Semver::getPatch,
          Semver::getPreRelease,
          Semver::getBuild
        )
        .containsExactly(0, 1, 1, Collections.emptyList(), Collections.emptyList());
    }
  }

  @Test
  void getMavenSnapshot() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      var pg = new SemverExtension(() -> Optional.of(git));

      var v000 = pg.getMavenSnapshot();
      assertThat(v000).extracting(Semver::getVersion).isEqualTo("0.0.0-SNAPSHOT");
      assertThat(v000)
        .hasToString("0.0.0-SNAPSHOT")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease)
        .containsExactly(0, 0, 0, List.of("SNAPSHOT"));

      git.tag().setName("v0.1.0").call();

      var v010 = pg.getMavenSnapshot();
      assertThat(v010).isGreaterThan(v000);

      git.commit().setMessage("second commit").call();

      var v010BldV = pg.getMavenSnapshot();

      assertThat(v010BldV).isGreaterThan(v000).isGreaterThan(v010).hasToString("0.1.1-SNAPSHOT");

      git.tag().setName("v0.1.1").call();

      var v011 = pg.getMavenSnapshot();

      assertThat(v011)
        .isGreaterThan(v010BldV)
        .isGreaterThan(v010)
        .isGreaterThan(v000)
        .hasToString("0.1.1")
        .extracting(
          Semver::getMajor,
          Semver::getMinor,
          Semver::getPatch,
          Semver::getPreRelease,
          Semver::getBuild
        )
        .containsExactly(0, 1, 1, Collections.emptyList(), Collections.emptyList());
    }
  }

  @Test
  void getMavenAlpha() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      var pg = new SemverExtension(() -> Optional.of(git));

      var v000 = pg.getMavenAlpha();
      assertThat(v000).extracting(Semver::getVersion).isEqualTo("0.0.1-alpha.1000000000000000");
      assertThat(v000)
        .hasToString("0.0.1-alpha.1000000000000000")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease)
        .containsExactly(0, 0, 1, List.of("alpha", "1000000000000000"));

      git.tag().setName("v0.1.0").call();
      var v010 = pg.getMavenAlpha();
      git.commit().setMessage("second commit").call();

      var v010BldV = pg.getMavenAlpha();

      assertThat(v010BldV)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o)
            .asInstanceOf(InstanceOfAssertFactories.STRING)
            .startsWith("0.1.1-alpha.1001")
            .matches("^0\\.1\\.1-alpha.1001\\p{XDigit}{12}$");
        });

      git.tag().setName("v0.1.1").call();

      var v011 = pg.getMavenAlpha();

      assertThat(v011)
        .isGreaterThan(v010BldV)
        .isGreaterThan(v010)
        .isGreaterThan(v000)
        .hasToString("0.1.1")
        .extracting(
          Semver::getMajor,
          Semver::getMinor,
          Semver::getPatch,
          Semver::getPreRelease,
          Semver::getBuild
        )
        .containsExactly(0, 1, 1, Collections.emptyList(), Collections.emptyList());
    }
  }

  @Test
  void mavenAlgorithm() {
    var pg = spy(new SemverExtension(() -> Optional.empty()));
    assertThat(pg.getMaven()).hasToString("0.0.0-SNAPSHOT");
    verify(pg, times(1)).getMavenSnapshot();
    verify(pg, never()).getMavenAlpha();
  }

  @Test
  void getVersionMismatchTag() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      git.commit().setMessage("initial commit").call();
      git.tag().setName("latest").call();
      git.commit().setMessage("second commit").call();

      var pg = new SemverExtension(() -> Optional.of(git));

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

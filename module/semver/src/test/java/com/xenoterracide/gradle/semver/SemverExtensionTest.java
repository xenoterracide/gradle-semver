// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static com.xenoterracide.gradle.semver.CommitTools.commit;
import static com.xenoterracide.gradle.semver.CommitTools.supplies;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.eclipse.jgit.api.Git;
import org.gradle.util.VersionNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semver4j.Semver;

class SemverExtensionTest {

  static final Pattern VERSION_PATTERN = Pattern.compile(
    "^\\d+\\.\\d+\\.\\d+-\\p{Alpha}+\\.\\d+\\.\\d+\\+\\p{XDigit}{8}$"
  );

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

      var v010BldV2 = pg.getGradlePlugin();

      assertThat(v010BldV2)
        .isGreaterThan(v000)
        .isGreaterThan(v010)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o)
            .asInstanceOf(InstanceOfAssertFactories.STRING)
            .startsWith("0.1.1-alpha.1+1.g")
            .matches("^0\\.1\\.1-alpha\\.1\\+\\d+\\.g\\p{XDigit}{7}$");
        });

      assertThat(v010BldV2).isEqualByComparingTo(new Semver("0.1.1-alpha.1+2.g3aae11e"));

      git.commit().setMessage("third commit").call();

      var v010BldV3 = pg.getGradlePlugin();

      assertThat(v010BldV3)
        .isGreaterThan(v000)
        .isGreaterThan(v010)
        .isGreaterThan(v010BldV2)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o).asInstanceOf(InstanceOfAssertFactories.STRING).startsWith("0.1.1-alpha" + ".2+2.g");
        });

      git.tag().setName("v0.1.1-rc.1").call();

      var v011Rc1 = pg.getGradlePlugin();

      assertThat(v011Rc1)
        .isGreaterThan(v000)
        .isGreaterThan(v010)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010BldV3)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o).asInstanceOf(InstanceOfAssertFactories.STRING).startsWith("0.1.1-rc.1");
        });

      git.tag().setName("v0.1.1").call();

      var v011 = pg.getGradlePlugin();

      assertThat(v011)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010)
        .isGreaterThan(v000)
        .hasToString("0.1.1")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease, Semver::getBuild)
        .containsExactly(0, 1, 1, Collections.emptyList(), Collections.emptyList());

      assertThat(VersionNumber.parse(v010BldV2.toString()))
        .isGreaterThan(VersionNumber.parse(v000.toString()))
        .isGreaterThan(VersionNumber.parse(v010.toString()))
        .isLessThan(VersionNumber.parse(v010BldV3.toString()))
        .isLessThan(VersionNumber.parse(v011.toString()));
    }
  }

  @Test
  void getGitDescribed() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      var pg = new SemverExtension(() -> Optional.of(git));
      Supplier<Semver> vs = pg::getVersion;

      var v001Alpha01 = supplies(commit(git), vs);

      assertThat(v001Alpha01).asString().startsWith("0.0.1-alpha.0.1+").hasSize(24).matches(VERSION_PATTERN);

      var v001Alpha02 = supplies(commit(git), vs);

      assertThat(v001Alpha02).asString().startsWith("0.0.1-alpha.0.2+").hasSize(24).matches(VERSION_PATTERN);

      git.tag().setName("v0.1.0").call();

      var v010 = vs.get();

      assertThat(v010).isGreaterThan(v001Alpha01);

      var v010BldV2 = supplies(commit(git), vs);

      assertThat(v010BldV2)
        .isGreaterThan(v001Alpha01)
        .isGreaterThan(v010)
        .asString()
        .startsWith("0.1.1-alpha.0.1+")
        .hasSize(24)
        .matches(VERSION_PATTERN);

      assertThat(v010BldV2).isEqualByComparingTo(new Semver("0.1.1-alpha.0.1+2.g3aae11e"));

      var v010BldV3 = supplies(commit(git), vs);

      assertThat(v010BldV3)
        .isGreaterThan(v001Alpha01)
        .isGreaterThan(v010)
        .isGreaterThan(v010BldV2)
        .asString()
        .startsWith("0.1.1-alpha.0.2+")
        .matches(VERSION_PATTERN);

      git.tag().setName("v0.1.1-rc.1").call();

      var v011Rc1 = vs.get();

      assertThat(v011Rc1)
        .isGreaterThan(v001Alpha01)
        .isGreaterThan(v010)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010BldV3)
        .asString()
        .isEqualTo("0.1.1-rc.1");

      git.tag().setName("v0.1.1").call();

      var v011 = pg.getVersion();

      assertThat(v011)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010)
        .isGreaterThan(v001Alpha01)
        .hasToString("0.1.1")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease, Semver::getBuild)
        .containsExactly(0, 1, 1, Collections.emptyList(), Collections.emptyList());

      assertThat(VersionNumber.parse(v010BldV2.toString()))
        .isGreaterThan(VersionNumber.parse(v001Alpha01.toString()))
        .isGreaterThan(VersionNumber.parse(v010.toString()))
        .isLessThan(VersionNumber.parse(v010BldV3.toString()))
        .isLessThan(VersionNumber.parse(v011.toString()));
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

      var v010BldV2 = pg.getMavenSnapshot();

      assertThat(v010BldV2).isGreaterThan(v000).isGreaterThan(v010).hasToString("0.1.1-SNAPSHOT");

      git.commit().setMessage("third commit").call();

      var v010BldV3 = pg.getMavenSnapshot();

      assertThat(v010BldV3)
        .isGreaterThan(v000)
        .isGreaterThan(v010)
        .isEqualByComparingTo(v010BldV2)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o).asInstanceOf(InstanceOfAssertFactories.STRING).startsWith("0.1.1-SNAPSHOT");
        });

      git.tag().setName("v0.1.1-rc.1").call();

      var v011Rc1 = pg.getMavenSnapshot();

      assertThat(v011Rc1)
        .isGreaterThan(v000)
        .isGreaterThan(v010)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010BldV3)
        .extracting(Semver::getVersion, Semver::toString)
        .allSatisfy(o -> {
          assertThat(o).asInstanceOf(InstanceOfAssertFactories.STRING).startsWith("0.1.1-rc.1");
        });

      git.tag().setName("v0.1.1").call();

      var v011 = pg.getMavenSnapshot();

      assertThat(v011)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010)
        .isGreaterThan(v000)
        .hasToString("0.1.1")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease, Semver::getBuild)
        .containsExactly(0, 1, 1, Collections.emptyList(), Collections.emptyList());

      assertThat(VersionNumber.parse(v010BldV2.toString()))
        .isGreaterThan(VersionNumber.parse(v000.toString()))
        .isGreaterThan(VersionNumber.parse(v010.toString()))
        .isEqualByComparingTo(VersionNumber.parse(v010BldV3.toString()))
        .isLessThan(VersionNumber.parse(v011.toString()));

      assertThat(new ComparableVersion(v010BldV2.toString()))
        .isGreaterThan(new ComparableVersion(v000.toString()))
        .isGreaterThan(new ComparableVersion(v010.toString()))
        .isEqualByComparingTo(new ComparableVersion(v010BldV3.toString()))
        .isLessThan(new ComparableVersion(v011.toString()));
    }
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

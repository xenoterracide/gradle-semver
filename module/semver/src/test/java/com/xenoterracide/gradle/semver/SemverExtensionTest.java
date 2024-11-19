// © Copyright 2018-2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static com.xenoterracide.gradle.semver.CommitTools.commit;
import static com.xenoterracide.gradle.semver.CommitTools.supplies;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
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
  void getGitDescribed() throws Exception {
    try (var git = Git.init().setDirectory(projectDir).call()) {
      var pg = new SemverExtension(() -> Optional.of(git));
      Supplier<Semver> vs = pg::getGitDescribed;

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

      var v011 = pg.getGitDescribed();

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
}

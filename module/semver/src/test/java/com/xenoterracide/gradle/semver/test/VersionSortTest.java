// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.semver4j.Semver;

/**
 * This is not a real test, but rather a way to experiment with version formatting and assumptions
 */
public class VersionSortTest {

  static String withBuild(String version, String... build) {
    var branch = !build[0].startsWith("g") ? "b" : "";
    return version + "+" + branch + String.join(".", build);
  }

  @ParameterizedTest
  @ArgumentsSource(MavenVersionList.class)
  @ArgumentsSource(RcVersionList.class)
  void maven(String greater, String than) {
    assertThat(new ComparableVersion(greater)).isGreaterThan(new ComparableVersion(than));
  }

  @ParameterizedTest
  @ArgumentsSource(RcVersionList.class)
  void semver(String greater, String than) {
    assertThat(new Semver(greater)).isGreaterThan(new Semver(than));
  }

  static class MavenVersionList implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      var version = "0.1.1-alpha.0.";
      var sha = "g3aae11c";
      return Stream.of(Arguments.of(withBuild(version + "1", sha), withBuild(version + "1", "topic-foo", sha)));
    }
  }

  static class RcVersionList implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      var sha = "g3aae11c";
      return Stream.of(
        Arguments.of("0.1.1-alpha.10.17129409589+6." + sha, "0.1.1-alpha.10+1.g3aae11d"),
        Arguments.of("0.1.1-alpha.10.17129409589+6.g3aae11c", "0.1.1-alpha.2.1712940957+2." + sha),
        Arguments.of("0.1.1-alpha.10.17129409589+6.g3aae11c", "0.1.1-alpha.6.1712940957+6." + sha),
        Arguments.of("0.1.1-alpha.10.17129409589+6.g3aae11c", "0.1.1-alpha.6.17129409589"),
        Arguments.of("0.1.1-alpha.0.17129409589+6.g3aae11c", "0.1.1-alpha.0.10+1.g3aae11d"),
        Arguments.of("0.1.1-alpha.0.17129409589+6.g3aae11c", "0.1.1-alpha.0.17129409578+2." + sha),
        Arguments.of("0.1.1-alpha.0.17129409589+6.g3aae11c", "0.1.1-alpha.0.17129409578+10" + sha),
        Arguments.of("0.1.1-alpha.0.17129409589+6.g3aae11c", "0.1.1-alpha.0.17129409588"),
        Arguments.of("0.1.1-rc.10.17129409589+6.g3aae11b", "0.1.1-alpha.0.10+1.g3aae11d"),
        Arguments.of("0.1.1-rc.10.17129409589+6.g3aae11b", "0.1.1-alpha.0.17129409578+2." + sha),
        Arguments.of("0.1.1-rc.10.17129409589+6.g3aae11b", "0.1.1-alpha.0.17129409578+10." + sha),
        Arguments.of("0.1.1-rc.10.17129409589+6.g3aae11b", "0.1.1-alpha.0.17129409588"),
        Arguments.of("0.1.1-rc.10.17129409589+6.g3aae11b", "0.1.1-rc.1.17129409588+6.g3aae11b"),
        Arguments.of("0.1.1-rc.10.17129409589+6.g3aae11b", "0.1.1-rc.1.17129409588"),
        Arguments.of("0.1.1-rc.10", "0.1.1-rc.9"),
        Arguments.of("0.10.10", "0.9.10"),
        Arguments.of("0.10.10", "0.9.10-rc.1"),
        Arguments.of("0.9.10", "0.9.1"),
        Arguments.of("1.1.1-rc.1.1+g3aae11b", "1.1.1-rc.1.0")
      );
    }
  }
}

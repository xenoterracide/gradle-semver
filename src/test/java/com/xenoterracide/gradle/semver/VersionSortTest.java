// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.gradle.util.VersionNumber;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.semver4j.Semver;

public class VersionSortTest {

  @ParameterizedTest
  @ArgumentsSource(RcVersionList.class)
  void maven(String thisVersion, String thatVersion) {
    assertThat(new ComparableVersion(thisVersion)).isGreaterThan(new ComparableVersion(thatVersion));
  }

  @Disabled
  @ParameterizedTest
  @ArgumentsSource(RcVersionList.class)
  void gradle(String thisVersion, String thatVersion) {
    assertThat(VersionNumber.parse(thisVersion)).isGreaterThan(VersionNumber.parse(thatVersion));
  }

  @ParameterizedTest
  @ArgumentsSource(RcVersionList.class)
  void semver(String thisVersion, String thatVersion) {
    assertThat(new Semver(thisVersion)).isGreaterThan(new Semver(thatVersion));
  }

  static class RcVersionList implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
        Arguments.of("0.1.1-alpha.10.17129409589+6.g3aae11c", "0.1.1-alpha.10+1.g3aae11d"),
        Arguments.of("0.1.1-alpha.10.17129409589+6.g3aae11c", "0.1.1-alpha.2.1712940957+2" + ".g3aae11c"),
        Arguments.of("0.1.1-alpha.10.17129409589+6.g3aae11c", "0.1.1-alpha.6.1712940957+6" + ".g3aae11c"),
        Arguments.of("0.1.1-alpha.10.17129409589+6.g3aae11c", "0.1.1-alpha.6.17129409589"),
        Arguments.of("0.1.1-alpha.0.17129409589+6.g3aae11c", "0.1.1-alpha.0.10+1.g3aae11d"),
        Arguments.of("0.1.1-alpha.0.17129409589+6.g3aae11c", "0.1.1-alpha.0.17129409578+2" + ".g3aae11c"),
        Arguments.of("0.1.1-alpha.0.17129409589+6.g3aae11c", "0.1.1-alpha.0.17129409578+10" + ".g3aae11c"),
        Arguments.of("0.1.1-alpha.0.17129409589+6.g3aae11c", "0.1.1-alpha.0.17129409588"),
        Arguments.of("0.1.1-rc.10.17129409589+6.g3aae11b", "0.1.1-alpha.0.10+1.g3aae11d"),
        Arguments.of("0.1.1-rc.10.17129409589+6.g3aae11b", "0.1.1-alpha.0.17129409578+2.g3aae11c"),
        Arguments.of("0.1.1-rc.10.17129409589+6.g3aae11b", "0.1.1-alpha.0.17129409578+10.g3aae11c"),
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

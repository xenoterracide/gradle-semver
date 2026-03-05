// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.semver4j.Semver;

/**
 * Tests version sorting with both Maven's ComparableVersion and Semver4j.
 *
 * <p>Important: Maven's ComparableVersion considers build metadata in comparison,
 * while Semver4j ignores it (per semver spec). Maven is the more accurate test
 * for repository resolution behavior.</p>
 */
public class VersionSortTest {

  static String withBuild(String version, String... build) {
    var branch = !build[0].startsWith("g") ? "b" : "";
    return version + "+" + branch + String.join(".", build);
  }

  @ParameterizedTest
  @ArgumentsSource(MavenVersionList.class)
  @ArgumentsSource(RcVersionList.class)
  @ArgumentsSource(DistanceVersionList.class)
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
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
      var version = "0.1.1-alpha.0.";
      var sha = "g3aae11c";
      return Stream.of(
        Arguments.of(withBuild(version + "1", sha), withBuild(version + "1", "topic-foo", sha)),
        Arguments.of("0.1.1-rc.1.2+branch.topic-foo.git.7.3aae11b", "0.1.1-rc.1.2+branch.topic-foo.git.6.3aae11b"),
        Arguments.of("0.1.1-rc.1.2+branch.topic-foo.git.70.3aae11b", "0.1.1-rc.1.2+branch.topic-foo.git.7.3aae11b"),
        Arguments.of("0.1.1-rc.1.3+branch.topic-foo.git.70.3aae11b", "0.1.1-rc.1.2+branch.topic-foo.git.70.3aae11b"),
        Arguments.of("0.1.1-rc.1.3+git.6.3aae11b", "0.1.1-rc.1.3+branch.topic-foo.git.70.3aae11b")
      );
    }
  }

  static class RcVersionList implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
      var sha = "3aae11c";
      return Stream.of(
        Arguments.of("0.1.1-alpha.10.17129409589+git.6." + sha, "0.1.1-alpha.10+git.1.3aae11d"),
        Arguments.of("0.1.1-alpha.10.17129409589+git.6.3aae11c", "0.1.1-alpha.2.1712940957+git.2." + sha),
        Arguments.of("0.1.1-alpha.10.17129409589+git.6.3aae11c", "0.1.1-alpha.6.1712940957+git.6." + sha),
        Arguments.of("0.1.1-alpha.10.17129409589+git.6.3aae11c", "0.1.1-alpha.6.17129409589"),
        Arguments.of("0.1.1-alpha.0.17129409589+git.6.3aae11c", "0.1.1-alpha.0.10+git.1.3aae11d"),
        Arguments.of("0.1.1-alpha.0.17129409589+git.6.3aae11c", "0.1.1-alpha.0.17129409578+git.2." + sha),
        Arguments.of("0.1.1-alpha.0.17129409589+git.6.3aae11c", "0.1.1-alpha.0.17129409578+10" + sha),
        Arguments.of("0.1.1-alpha.0.17129409589+git.6.3aae11c", "0.1.1-alpha.0.17129409588"),
        Arguments.of("0.1.1-rc.10.17129409589+git.6.3aae11b", "0.1.1-alpha.0.10+git.1.3aae11d"),
        Arguments.of("0.1.1-rc.10.17129409589+git.6.3aae11b", "0.1.1-alpha.0.17129409578+git.2." + sha),
        Arguments.of("0.1.1-rc.10.17129409589+git.6.3aae11b", "0.1.1-alpha.0.17129409578+git.10." + sha),
        Arguments.of("0.1.1-rc.10.17129409589+git.6.3aae11b", "0.1.1-alpha.0.17129409588"),
        Arguments.of("0.1.1-rc.10.17129409589+git.6.3aae11b", "0.1.1-rc.1.17129409588+git.6.3aae11b"),
        Arguments.of("0.1.1-rc.10.17129409589+git.6.3aae11b", "0.1.1-rc.1.17129409588+branch.topic-foo.git.6.3aae11b"),
        Arguments.of("0.1.1-rc.10.17129409589+git.6.3aae11b", "0.1.1-rc.1.17129409588"),
        Arguments.of("0.1.1-rc.10", "0.1.1-rc.9"),
        Arguments.of("0.10.10", "0.9.10"),
        Arguments.of("0.10.10", "0.9.10-rc.1"),
        Arguments.of("0.9.10", "0.9.1"),
        Arguments.of("1.1.1-rc.1.1+git.3aae11b", "1.1.1-rc.1.0")
      );
    }
  }

  /**
   * Tests distance-based versioning scheme used by the plugin.
   * Verifies that HEAD branch and topic branch versions sort correctly.
   */
  static class DistanceVersionList implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
      var sha = "3aae11b";
      return Stream.of(
        // Exact tag is always lowest
        Arguments.of("1.0.0+git.0." + sha, "1.0.0"),
        Arguments.of("1.0.0+branch.topic.git.0." + sha, "1.0.0"),
        // After stable tag: HEAD branch versions
        Arguments.of("1.0.1-alpha.0.5+git.5." + sha, "1.0.0"),
        Arguments.of("1.0.1-alpha.0.5+git.5." + sha, "1.0.1-alpha.0.3+git.3." + sha),
        Arguments.of("1.0.1-alpha.0.10+git.10." + sha, "1.0.1-alpha.0.5+git.5." + sha),
        // After stable tag: topic branch vs HEAD branch (same prerelease, different metadata)
        // HEAD branch should sort higher than topic branch for same base version
        Arguments.of("1.0.1-alpha.0.5+git.5." + sha, "1.0.1-alpha.0.5+branch.topic.git.3." + sha),
        // Topic branch with more commits should sort higher (metadata comparison)
        Arguments.of("1.0.1-alpha.0.5+branch.topic.git.10." + sha, "1.0.1-alpha.0.5+branch.topic.git.3." + sha),
        // After prerelease tag: extending the prerelease
        Arguments.of("1.0.0-rc.1.5+git.5." + sha, "1.0.0-rc.1"),
        Arguments.of("1.0.0-rc.1.5+git.5." + sha, "1.0.0-rc.1.3+git.3." + sha),
        Arguments.of("1.0.0-rc.1.5+git.5." + sha, "1.0.0-rc.1.5+branch.topic.git.3." + sha),
        // Stable tag beats prerelease
        Arguments.of("1.0.0", "1.0.0-rc.1.10+git.10." + sha),
        Arguments.of("1.0.0", "1.0.0-alpha.0.100+git.100." + sha),
        // Version progression: tag < HEAD after tag < topic after tag
        Arguments.of("1.0.1-alpha.0.1+git.1." + sha, "1.0.0"),
        Arguments.of("1.0.1-alpha.0.1+branch.topic.git.1." + sha, "1.0.0"),
        Arguments.of("1.0.1-alpha.0.1+git.1." + sha, "1.0.1-alpha.0.1+branch.topic.git.1." + sha)
      );
    }
  }
}

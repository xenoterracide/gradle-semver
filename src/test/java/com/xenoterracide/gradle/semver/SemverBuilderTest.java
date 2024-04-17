// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.semver4j.Semver;

class SemverBuilderTest {

  @ParameterizedTest
  @ArgumentsSource(VersionProvider.class)
  void gitMetadata(
    GitMetadata gitMetadata,
    String expected,
    String comp,
    String lessThan,
    @Nullable String greaterThan
  ) {
    var semv = new SemverBuilder(gitMetadata).build();
    assertThat(semv).describedAs("equal").isEqualTo(new Semver(expected));
    assertThat(semv).describedAs("comparing").isEqualByComparingTo(new Semver(comp));
    assertThat(semv).describedAs("lessThan").isLessThan(new Semver(lessThan));
    assertThat(new ComparableVersion(semv.toString())).describedAs("mvn").isLessThan(new ComparableVersion(lessThan));

    if (greaterThan != null) {
      assertThat(new ComparableVersion(semv.toString()))
        .describedAs("mvn")
        .isGreaterThan(new ComparableVersion(greaterThan));
    }
  }

  /*


  @Test
  void rcTag() {
    assertThat(new SemverBuilder(new GitMetadataInfo(0, "abcdef10", "v1.0.0-rc.1")).build())
    .isEqualTo(
      new Semver("1.0.0").withPreRelease("rc.1.0").withBuild("abcdef10")
    );
  }

  @Test
  void rcTagPlus1() {
    assertThat(new SemverBuilder(new GitMetadataInfo(1, "abcdef10", "v1.0.0-rc.1")).build())
    .isEqualTo(
      new Semver("1.0.0").withPreRelease("rc.1.1").withBuild("abcdef10")
    );
  }

  @Test
  void alphaTag() {
    assertThat(new SemverBuilder(new GitMetadataInfo(0, "abcdef10", "v1.0.0-alpha.1")).build())
    .isEqualTo(
      new Semver("1.0.0").withPreRelease("alpha.1.0").withBuild("abcdef10")
    );
  }

  @Test
  void stableTag() {
    assertThat(new SemverBuilder(new GitMetadataInfo(0, "abcdef10", "v1.0.0")).build()).isEqualTo(
      new Semver("1.0.0").withBuild("abcdef10")
    );
  }

   */

  static class VersionProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
        arguments(
          new GitMetadataInfo(0, GitStatus.NO_REPO, null, null),
          "0.0.0-alpha.0.0",
          "0.0.0-alpha.0.0",
          "0.0.0-alpha.0.1",
          null
        ),
        arguments(
          new GitMetadataInfo(1, GitStatus.CLEAN, "abcdef10", null),
          "0.0.0-alpha.0.1+abcdef10",
          "0.0.0-alpha.0.1",
          "0.0.0-alpha.0.2",
          null
        ),
        arguments(
          new GitMetadataInfo(1, GitStatus.DIRTY, "abcdef10", null),
          "0.0.0-alpha.0.1+abcdef10.dirty",
          "0.0.0-alpha.0.1",
          "0.0.0-alpha.0.2",
          "0.0.0-alpha.0.0"
        ),
        arguments(
          new GitMetadataInfo(1, GitStatus.CLEAN, "abcdef10", null),
          "0.0.0-alpha.0.1+abcdef10",
          "0.0.0-alpha.0.1",
          "0.0.0-alpha.1.0",
          "0.0.0-alpha.0.0"
        ),
        arguments(
          new GitMetadataInfo(10, GitStatus.CLEAN, "abcdef10", null),
          "0.0.0-alpha.0.10+abcdef10",
          "0.0.0-alpha.0.10",
          "0.0.0-alpha.0.11",
          "0.0.0-alpha.0.1"
        ),
        arguments(
          new GitMetadataInfo(0, GitStatus.CLEAN, "abcdef10", "v1.0.0-rc.1"),
          "1.0.0-rc.1",
          "1.0.0-rc.1",
          "1.0.0",
          "1.0.0-alpha.1"
        )
      );
    }
  }

  record GitMetadataInfo(int distance, GitStatus status, @Nullable String uniqueShort, @Nullable String tag)
    implements GitMetadata {}
}

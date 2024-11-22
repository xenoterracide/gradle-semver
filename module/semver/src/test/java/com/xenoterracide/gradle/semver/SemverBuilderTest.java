// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.xenoterracide.gradle.semver.internal.GitMetadata;
import java.util.List;
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
    var semv = new SemverBuilder(gitMetadata).withDirtyOut(true).build();
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
          new GitMetadataInfo(1, GitStatus.CLEAN, "abcdef11", null),
          "0.0.1-alpha.0.1+gabcdef11",
          "0.0.1-alpha.0.1",
          "0.0.1-alpha.0.2",
          null
        ),
        arguments(
          new GitMetadataInfo(1, GitStatus.DIRTY, "abcdef12", null),
          "0.0.1-alpha.0.1+gabcdef12.dirty",
          "0.0.1-alpha.0.1",
          "0.0.1-alpha.0.2",
          "0.0.1-alpha.0.0"
        ),
        arguments(
          new GitMetadataInfo(1, GitStatus.CLEAN, "abcdef13", null),
          "0.0.1-alpha.0.1+gabcdef13",
          "0.0.1-alpha.0.1",
          "0.0.1-alpha.1.0",
          "0.0.1-alpha.0.0"
        ),
        arguments(
          new GitMetadataInfo(10, GitStatus.CLEAN, "abcdef14", null),
          "0.0.1-alpha.0.10+gabcdef14",
          "0.0.1-alpha.0.10",
          "0.0.1-alpha.0.11",
          "0.0.1-alpha.0.1"
        ),
        arguments(
          new GitMetadataInfo(0, GitStatus.CLEAN, "abcdef15", "v1.0.0-rc.1"),
          "1.0.0-rc.1",
          "1.0.0-rc.1",
          "1.0.0",
          "1.0.0-alpha.1"
        ),
        arguments(
          new GitMetadataInfo(1, GitStatus.CLEAN, "abcdef16", "v1.0.0-rc.1"),
          "1.0.0-rc.1.1+gabcdef16",
          "1.0.0-rc.1.1",
          "1.0.0-rc.2",
          "1.0.0-rc.1"
        ),
        arguments(
          new GitMetadataInfo(0, GitStatus.CLEAN, "abcdef17", "v1.0.0"),
          "1.0.0",
          "1.0.0",
          "1.0.1",
          "1.0.0-rc.1"
        ),
        arguments(
          new GitMetadataInfo(1, GitStatus.CLEAN, "abcdef18", "v1.0.0"),
          "1.0.1-alpha.0.1+gabcdef18",
          "1.0.1-alpha.0.1",
          "1.0.1",
          "1.0.1-alpha.0"
        )
      );
    }
  }

  record GitMetadataInfo(int distance, GitStatus status, @Nullable String uniqueShort, @Nullable String tag)
    implements GitMetadata {
    @Override
    public @Nullable String branch() {
      return "";
    }

    @Override
    public @Nullable String commit() {
      return "";
    }

    @Override
    public List<GitRemote> remotes() {
      return List.of();
    }
  }
}

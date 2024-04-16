// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.semver4j.Semver;

class SemverBuilderTest {

  @Test
  void noCommit() {
    var semver = new SemverBuilder(new GitMetadataInfo(0, null, null)).build();
    assertThat(semver).isEqualTo(new Semver("0.0.0").withPreRelease("alpha.0.0"));
  }

  @Test
  void firstCommit() {
    var semver = new SemverBuilder(new GitMetadataInfo(0, "abcdef10", null)).build();
    assertThat(semver).isEqualTo(new Semver("0.0.0").withPreRelease("alpha.0.0").withBuild("abcdef10"));
  }

  @Test
  void secondCommit() {
    var semver = new SemverBuilder(new GitMetadataInfo(1, "abcdef10", null)).build();
    assertThat(semver).isEqualTo(new Semver("0.0.0").withPreRelease("alpha.0.1").withBuild("abcdef10"));
  }

  @Test
  void tenthCommit() {
    var semver = new SemverBuilder(new GitMetadataInfo(10, "abcdef10", null)).build();
    assertThat(semver).isEqualTo(new Semver("0.0.0").withPreRelease("alpha.0.10").withBuild("abcdef10"));
  }

  @Test
  void rcTag() {
    assertThat(new SemverBuilder(new GitMetadataInfo(0, "abcdef10", "v1.0.0-rc.1")).build()).isEqualTo(
      new Semver("1.0.0").withPreRelease("rc.1.0").withBuild("abcdef10")
    );
  }

  @Test
  void rcTagPlus1() {
    assertThat(new SemverBuilder(new GitMetadataInfo(1, "abcdef10", "v1.0.0-rc.1")).build()).isEqualTo(
      new Semver("1.0.0").withPreRelease("rc.1.1").withBuild("abcdef10")
    );
  }

  @Test
  void alphaTag() {
    assertThat(new SemverBuilder(new GitMetadataInfo(0, "abcdef10", "v1.0.0-alpha.1")).build()).isEqualTo(
      new Semver("1.0.0").withPreRelease("alpha.1.0").withBuild("abcdef10")
    );
  }

  @Test
  void stableTag() {
    assertThat(new SemverBuilder(new GitMetadataInfo(0, "abcdef10", "v1.0.0")).build()).isEqualTo(
      new Semver("1.0.0").withBuild("abcdef10")
    );
  }

  record GitMetadataInfo(int distance, @Nullable String uniqueShort, @Nullable String tag) implements GitMetadata {}
}

// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.xenoterracide.gradle.git.GitMetadata;
import com.xenoterracide.gradle.git.GitRemote;
import com.xenoterracide.gradle.git.GitStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.eclipse.jgit.lib.Constants;
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
  void dirty(
    GitMetadata gitMetadata,
    String expected,
    String comp,
    String lessThan,
    long headBranchDistance,
    @Nullable String greaterThan
  ) {
    var semv = new SemverBuilder(Semver.ZERO).withDirtyOut(true).build();
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

  // NOTE: the last 2 characters in the unique short denote the position in the array for debugging
  static class VersionProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
        arguments(
          new GitMetadataInfoNoBranch(0, GitStatus.NO_REPO, null, null),
          "0.0.0-alpha.0.0",
          "0.0.0-alpha.0.0",
          "0.0.0-alpha.0.1",
          0L,
          null
        ),
        arguments(
          new GitMetadataInfoNoBranch(1, GitStatus.CLEAN, "abcdef02", null),
          "0.0.1-alpha.0.1+gabcdef02",
          "0.0.1-alpha.0.1",
          "0.0.1-alpha.0.2",
          0L,
          null
        ),
        arguments(
          new GitMetadataInfoNoBranch(1, GitStatus.DIRTY, "abcdef03", null),
          "0.0.1-alpha.0.1+gabcdef03.dirty",
          "0.0.1-alpha.0.1",
          "0.0.1-alpha.0.2",
          0L,
          "0.0.1-alpha.0.0"
        ),
        arguments(
          new GitMetadataInfoNoBranch(1, GitStatus.CLEAN, "abcdef04", null),
          "0.0.1-alpha.0.1+gabcdef04",
          "0.0.1-alpha.0.1",
          "0.0.1-alpha.1.0",
          0L,
          "0.0.1-alpha.0.0"
        ),
        arguments(
          new GitMetadataInfoNoBranch(10, GitStatus.CLEAN, "abcdef05", null),
          "0.0.1-alpha.0.10+gabcdef05",
          "0.0.1-alpha.0.10",
          "0.0.1-alpha.0.11",
          0L,
          "0.0.1-alpha.0.1"
        ),
        arguments(
          new GitMetadataInfoNoBranch(0, GitStatus.CLEAN, "abcdef06", "v1.0.0-rc.1"),
          "1.0.0-rc.1",
          "1.0.0-rc.1",
          "1.0.0",
          0L,
          "1.0.0-alpha.1"
        ),
        arguments(
          new GitMetadataInfoNoBranch(1, GitStatus.CLEAN, "abcdef07", "v1.0.0-rc.1"),
          "1.0.0-rc.1.1+gabcdef07",
          "1.0.0-rc.1.1",
          "1.0.0-rc.2",
          0L,
          "1.0.0-rc.1"
        ),
        arguments(
          new GitMetadataInfoNoBranch(0, GitStatus.CLEAN, "abcdef08", "v1.0.0"),
          "1.0.0",
          "1.0.0",
          "1.0.1",
          0L,
          "1.0.0-rc.1"
        ),
        arguments(
          new GitMetadataInfoNoBranch(1, GitStatus.CLEAN, "abcdef09", "v1.0.0"),
          "1.0.1-alpha.0.1+gabcdef09",
          "1.0.1-alpha.0.1",
          "1.0.1",
          0L,
          "1.0.1-alpha.0"
        ),
        arguments(
          GitMetadataInfoBranch.create(
            1,
            GitStatus.CLEAN,
            "abcdef10",
            "v1.0.0",
            Map.of("origin", "main", "upstream", "foo")
          ),
          "1.0.1-alpha.0.1+btopic-foo.gabcdef10",
          "1.0.1-alpha.0.1",
          "1.0.1",
          1L,
          "1.0.1-alpha.0"
        )
      );
    }
  }

  record GitMetadataInfoNoBranch(long distance, GitStatus status, @Nullable String uniqueShort, @Nullable String tag)
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

  record GitRemoteImpl(String name, String headBranch) implements GitRemote {
    static GitRemote create(String name, String headBranch) {
      return new GitRemoteImpl(name, Constants.R_REMOTES + name + "/" + headBranch);
    }
  }

  record GitMetadataInfoBranch(
    long distance,
    GitStatus status,
    @Nullable String uniqueShort,
    @Nullable String tag,
    List<GitRemote> remotes
  )
    implements GitMetadata {
    static GitMetadata create(
      long distance,
      GitStatus status,
      @Nullable String uniqueShort,
      @Nullable String tag,
      Map<String, String> remoteMap
    ) {
      var remotes = remoteMap
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue())
        .map(entry -> GitRemoteImpl.create(entry.getKey(), entry.getValue()))
        .toList();
      return new GitMetadataInfoBranch(distance, status, uniqueShort, tag, remotes);
    }

    @Override
    public @Nullable String branch() {
      return "topic/foo";
    }

    @Override
    public @Nullable String commit() {
      return "";
    }
  }
}

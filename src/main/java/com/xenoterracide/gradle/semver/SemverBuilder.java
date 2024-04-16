// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.google.common.base.Splitter;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

final class SemverBuilder {

  static final String GIT_DESCRIBE_DELIMITER = "-";

  private static final Splitter DESCRIBE_SPLITTER = Splitter.on(GIT_DESCRIBE_DELIMITER);

  private static final String PRE_VERSION = "0.0.0";
  private static final String SNAPSHOT = "SNAPSHOT";
  private static final String ALPHA = "alpha";
  private static final String SEMVER_DELIMITER = ".";
  private static final Pattern GIT_DESCRIBE_PATTERN = Pattern.compile("^\\d+-+g\\p{XDigit}{7}$");

  private static final String ZERO = "0";

  private final GitMetadata gitMetadata;
  private Semver semver;

  SemverBuilder(GitMetadata gitMetadata) {
    this.gitMetadata = gitMetadata;
    this.semver = new Semver(tagFrom(this.gitMetadata.tag()));
  }

  static Semver movePrereleaseToBuild(Semver version) {
    if (version.getPreRelease().stream().anyMatch(GIT_DESCRIBE_PATTERN.asMatchPredicate())) {
      var buildInfo = Splitter.on(GIT_DESCRIBE_DELIMITER).splitToList(
        String.join(GIT_DESCRIBE_DELIMITER, version.getPreRelease())
      );
      return version
        .withClearedPreReleaseAndBuild()
        .withIncPatch()
        .withPreRelease(String.join(SEMVER_DELIMITER, ALPHA, buildInfo.get(0)))
        .withBuild(String.join(SEMVER_DELIMITER, buildInfo));
    }
    return version;
  }

  static String tagFrom(@Nullable String vString) {
    return vString == null ? PRE_VERSION : vString.substring(1);
  }

  private SemverBuilder withPreRelease() {
    var distance = this.gitMetadata.distance();
    if (!this.semver.getPreRelease().isEmpty()) { // rc.1
      var preRelease = Stream.concat(
        this.semver.getPreRelease().stream(),
        Stream.of(Integer.toString(distance))
      ).collect(Collectors.joining(SEMVER_DELIMITER));
      this.semver = this.semver.withClearedPreRelease().withPreRelease(preRelease);
    }
    if (this.semver.getMajor() == 0 && this.semver.getMinor() == 0 && this.semver.getPatch() == 0) {
      this.semver = this.semver.withPreRelease(String.join(SEMVER_DELIMITER, ALPHA, ZERO, Integer.toString(distance)));
    }
    return this;
  }

  private SemverBuilder withBuild() {
    var sha = Optional.ofNullable(this.gitMetadata.uniqueShort());
    this.semver = sha.map(s -> this.semver.withBuild(s)).orElse(this.semver);
    return this;
  }

  Semver build() {
    this.withPreRelease().withBuild();
    return this.semver;
  }
}

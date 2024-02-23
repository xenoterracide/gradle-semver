// SPDX-License-Identifier: Apache-2.0
// © Copyright 2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver;

import io.vavr.control.Try;
import java.util.Objects;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

public class SemverExtension {

  // this is not a regex but a glob (`man glob`)
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";
  private static final String PRE_VERSION = "0.0.0";
  private static final String SNAPSHOT = "SNAPSHOT";

  private final Git git;

  SemverExtension(@NonNull Git git) {
    this.git = Objects.requireNonNull(git);
  }

  Try<@Nullable String> describe() {
    return Try
      .of(() -> this.git.describe().setMatch(VERSION_GLOB))
      .mapTry(DescribeCommand::call)
      .onFailure(ExceptionTools::rethrow);
  }

  public Semver getGradlePlugin() {
    return describe()
      .map(v -> null == v ? PRE_VERSION : v)
      .map(Semver::coerce)
      .map(v ->
        !(v.getPreRelease().isEmpty() || v.getBuild().isEmpty()) ? v.withClearedPreReleaseAndBuild().nextPatch() : v
      )
      .get();
  }

  public Semver getMaven() {
    return describe()
      .map(v -> null == v ? PRE_VERSION : v)
      .map(Semver::coerce)
      .map(v -> Objects.equals(v.getVersion(), PRE_VERSION) ? v.withPreRelease(SNAPSHOT) : v)
      .map(v ->
        v
          .getPreRelease()
          .stream()
          .filter(p -> p.matches("^\\d+-+g\\p{XDigit}{7}$"))
          .findFirst()
          .map(p -> v.withClearedPreRelease().withPreRelease(SNAPSHOT).withBuild(p))
          .orElse(v)
      )
      .map(v -> new MavenSemver(v.getVersion()))
      .get();
  }
}

// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.ExceptionTools;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.function.Supplier;
import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

/**
 * The type Semver extension.
 */
public class SemverExtension {

  // this is not a regex but a glob (`man glob`)
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";
  private static final String PRE_VERSION = "0.0.0";
  private static final String SNAPSHOT = "SNAPSHOT";

  private final Supplier<Git> git;

  /**
   * Instantiates a new Semver extension.
   *
   * @param git {@link Supplier} of {@link Git}
   */
  public SemverExtension(@NonNull Supplier<@NonNull Git> git) {
    this.git = Objects.requireNonNull(git);
  }

  Try<@Nullable String> describe() {
    return Try.of(() -> this.git.get().describe().setMatch(VERSION_GLOB).setTags(true))
      .mapTry(DescribeCommand::call)
      .onFailure(ExceptionTools::rethrow);
  }

  /**
   * Gets git metatdata exstension.
   *
   * @implNote does not invoke {@link org.eclipse.jgit.lib.Repository}
   *
   * @return the extension for accessing git metdata
   */
  public GitMetadataExtension getGit() {
    return new GitMetadataExtension(this.git);
  }

  /**
   * Gets gradle plugin compatible version.
   *
   * @implNote Actually invokes {@link org.eclipse.jgit.lib.Repository}
   *
   * @return the gradle plugin semver.
   */
  public Semver getGradlePlugin() {
    return this.describe()
      .map(v -> null == v ? PRE_VERSION : v)
      .map(Semver::coerce)
      .map(
        v ->
          !(v.getPreRelease().isEmpty() || v.getBuild().isEmpty()) ? v.withClearedPreReleaseAndBuild().nextPatch() : v
      )
      .get();
  }

  /**
   * Gets maven compatible version.
   *
   * @implNote Actually invokes {@link org.eclipse.jgit.lib.Repository}
   *
   * @return the maven compatible semver
   */
  public Semver getMaven() {
    return this.describe()
      .map(v -> null == v ? PRE_VERSION : v)
      .map(Semver::coerce)
      .map(v -> Objects.equals(v.getVersion(), PRE_VERSION) ? v.withPreRelease(SNAPSHOT) : v)
      .map(
        v ->
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

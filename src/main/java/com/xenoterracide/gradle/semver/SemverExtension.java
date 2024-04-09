// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.ExceptionTools;
import com.xenoterracide.gradle.semver.internal.GitTools;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.jspecify.annotations.NonNull;
import org.semver4j.Semver;

/**
 * The type Semver extension. Methods in this class are not lazy and invoke the {@link org.eclipse.jgit.lib.Repository}.
 * All versions returned are Gradle safe as they are all valid semantic versions.
 */
public class SemverExtension {

  private static final String PRE_VERSION = "0.0.0";
  private static final String SNAPSHOT = "SNAPSHOT";
  private static final String ALPHA = "alpha";

  private final Supplier<Optional<Git>> git;

  /**
   * Instantiates a new Semver extension.
   *
   * @param git {@link Supplier} of {@link Git}
   */
  public SemverExtension(@NonNull Supplier<Optional<Git>> git) {
    this.git = git;
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

  Try<Semver> coerced() {
    return this.getGit()
      .describe()
      .map(v -> null == v ? PRE_VERSION : v)
      .map(Semver::coerce)
      .filter(Objects::nonNull);
  }

  /**
   * Gets gradle plugin compatible version.
   *
   * @implNote Actually invokes {@link org.eclipse.jgit.lib.Repository}
   *
   * @return the gradle plugin semver.
   */
  public Semver getGradlePlugin() {
    return this.coerced()
      .map(
        v ->
          !(v.getPreRelease().isEmpty() || v.getBuild().isEmpty())
            ? v.withClearedPreReleaseAndBuild().nextPatch()
            : v
      )
      .get();
  }

  /**
   * Gets maven compatible version.
   *
   * @implNote Actually invokes {@link org.eclipse.jgit.lib.Repository}.
   * Uses the {@link #getMavenSnapshot()} algorithm, will switch to {@link #getMavenAlpha()} in the future.
   *
   * @return the maven compatible semver
   */
  public Semver getMaven() {
    return this.getMavenSnapshot();
  }

  public Semver getMavenSnapshot() {
    return this.coerced()
      .map(v -> Objects.equals(v.getVersion(), PRE_VERSION) ? v.withPreRelease(SNAPSHOT) : v)
      .map(
        v ->
          v
            .getPreRelease()
            .stream()
            .filter(p -> p.matches("^\\d+-+g\\p{XDigit}{7}$"))
            .findAny()
            .map(p -> v.withClearedPreRelease().withPreRelease(SNAPSHOT).withBuild(p))
            .orElse(v)
      )
      .map(v -> new MavenSemver(v.getVersion()))
      .get();
  }

  String octalForHead() {
    var oid = this.getGit().getObjectIdFor(Constants.HEAD).get();
    return GitTools.toOctal(oid, 4);
  }

  String preRelease(int distance) {
    var delimeter = ".";
    var octalSha = distance > 0 ? this.octalForHead() : "0".repeat(12);
    var dist = distance + 1000;

    return String.join(delimeter, ALPHA, dist + octalSha);
  }

  /**
   * Maven Compatible version that uses alpha instead of snapshot. It can be locked by gradle released every build.
   *
   * @implNote current algorithm for alphas is semver + alpha + (distance + 1000) + 4 byte octal of commit
   * We add 1000 to the distance because a valid numeric in the prerelease cannot have a leading 0 and maven uses a
   * stringy comparison of this number instead of an integer. This means that you shoud be fine until you reach 10000
   * commits between releases.
   *
   * @return maven compatible semver
   */
  public Semver getMavenAlpha() {
    return this.getGit()
      .describe()
      .map(v -> null == v ? PRE_VERSION : v)
      .map(this::toAlpha)
      .getOrElseThrow(ExceptionTools::rethrow);
  }

  Semver toAlpha(String version) {
    var distance = this.getGit().getCommitDistance();

    var semver = Objects.requireNonNull(Semver.coerce(version));
    if (distance > 0 || PRE_VERSION.equals(version)) {
      return semver.withPreRelease(this.preRelease(distance));
    }

    return semver;
  }
}

// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.google.common.base.Splitter;
import com.xenoterracide.gradle.semver.internal.ExceptionTools;
import com.xenoterracide.gradle.semver.internal.GitTools;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.jspecify.annotations.NonNull;
import org.semver4j.Semver;

/**
 * The Semver extension.
 *
 * @implNote pre-release versions between branches which have the same git commit distance are not
 *   guaranteed to sort correctly and would do so only by coincidence.
 *
 * @implNote Methods in this class are not lazy and invoke the
 *   {@link org.eclipse.jgit.lib.Repository}. All versions returned are Gradle safe as they are all
 *   valid semantic versions.
 */
public class SemverExtension {

  private static final String PRE_VERSION = "0.0.0";
  private static final String SNAPSHOT = "SNAPSHOT";
  private static final String ALPHA = "alpha";
  private static final String SEMVER_DELIMITER = ".";
  private static final String GIT_DESCRIBE_DELIMITER = "-";
  private static final Pattern GIT_DESCRIBE_PATTERN = Pattern.compile("^\\d+-+g\\p{XDigit}{7}$");

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
   * @return the extension for accessing git metdata
   * @implNote does not invoke {@link org.eclipse.jgit.lib.Repository}
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
   * {@snippet :
   * logger.quiet("gradlePlugin" + semver.gradlePlugin)  // 0.1.1-alpha.1+1.g3aae11e
   *}
   *
   * @return the gradle plugin semver.
   * @implNote Actually invokes {@link org.eclipse.jgit.lib.Repository}
   */
  public Semver getGradlePlugin() {
    return this.coerced()
      .map(v -> {
        if (v.getPreRelease().stream().anyMatch(GIT_DESCRIBE_PATTERN.asMatchPredicate())) {
          var buildInfo = Splitter.on(GIT_DESCRIBE_DELIMITER).splitToList(
            String.join(GIT_DESCRIBE_DELIMITER, v.getPreRelease())
          );
          return v
            .withClearedPreReleaseAndBuild()
            .withIncPatch()
            .withPreRelease(String.join(SEMVER_DELIMITER, ALPHA, buildInfo.get(0)))
            .withBuild(String.join(SEMVER_DELIMITER, buildInfo));
        }
        return v;
      })
      .get();
  }

  /**
   * Gets maven compatible version.
   *
   * @return the maven compatible semver
   * @implNote Uses the {@link #getMavenSnapshot()} algorithm, may switch to
   *   {@link #getMavenAlpha()} in the future.
   */
  public Semver getMaven() {
    return this.getMavenSnapshot();
  }

  /**
   * Traditional maven snapshot version.
   * {@snippet :
   * logger.quiet("maven snapshot"+semver.mavenSnapshot) // 0.1.1-SNAPSHOT
   *}
   *
   * @return maven compatible semver
   * @implNote The current algorith removes the pre-release information and instead appeads with
   *   {@code "SNAPSHOT"} if the commit distance is greater than 0.
   */
  public Semver getMavenSnapshot() {
    return this.coerced()
      .map(v -> Objects.equals(v.getVersion(), PRE_VERSION) ? v.withPreRelease(SNAPSHOT) : v)
      .map(
        v ->
          v
            .getPreRelease()
            .stream()
            .filter(GIT_DESCRIBE_PATTERN.asMatchPredicate())
            .findAny()
            .map(p -> v.withClearedPreReleaseAndBuild().nextPatch().withPreRelease(SNAPSHOT))
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
    var octalSha = distance > 0 ? this.octalForHead() : "0".repeat(12);
    var dist = distance + 1000;

    return String.join(SEMVER_DELIMITER, ALPHA, dist + octalSha);
  }

  /**
   * Maven Compatible version that uses alpha instead of snapshot. It can be locked by gradle
   * released every build.
   * {@snippet :
   * logger.quiet("maven alpha " + semver.mavenAlpha) // 0.1.1-alpha.1001255204163142
   *}
   *
   * @return maven compatible semver
   * @implNote current algorithm for alphas is semver + alpha + (distance + 1000) + 4 byte octal
   *   of commit We add 1000 to the distance because a valid numeric in the prerelease cannot have a
   *   leading 0 and maven uses a stringy comparison of this number instead of an integer. This
   *   means that you shoud be fine until you reach 9000 commits between releases.
   */
  public Semver getMavenAlpha() {
    return this.getGit()
      .describe()
      .map(v -> null == v ? PRE_VERSION : v)
      .map(this::toAlpha)
      .map(v -> new MavenSemver(v.getVersion()))
      .getOrElseThrow(ExceptionTools::rethrow);
  }

  Semver toAlpha(String version) {
    var distance = this.getGit().getCommitDistance();

    var semver = Objects.requireNonNull(Semver.coerce(version));
    if (distance > 0 || PRE_VERSION.equals(version)) {
      return semver.withPreRelease(this.preRelease(distance)).withIncPatch();
    }

    return semver;
  }
}

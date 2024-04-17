// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static com.xenoterracide.gradle.semver.SemverBuilder.ALPHA;
import static com.xenoterracide.gradle.semver.SemverBuilder.SEMVER_DELIMITER;

import com.google.common.base.Splitter;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.eclipse.jgit.api.Git;
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
  private static final Pattern GIT_DESCRIBE_PATTERN = Pattern.compile("^\\d+-+g\\p{XDigit}{7}$");
  private static final String GIT_DESCRIBE_DELIMITER = "-";

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
    return this.getGit().describe().map(v -> null == v ? PRE_VERSION : v).map(Semver::coerce).filter(Objects::nonNull);
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

  /**
   * Gets gradle plugin compatible version.
   * {@snippet :
   * logger.quiet("gradlePlugin" + semver.gradlePlugin)  // 0.1.1-alpha.1+1.g3aae11e
   *}
   *
   * @implNote will probably delegate to {@link #gitDescribed()} in the future.
   * @return the gradle plugin semver.
   */
  public Semver getGradlePlugin() {
    return this.coerced().map(SemverExtension::movePrereleaseToBuild).get();
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
      .map(v -> new Semver(v.getVersion()))
      .get();
  }

  /**
   * Gets maven compatible version.
   *
   * @implNote currently delegates to {@link #getMavenSnapshot()} will probably delegate to
   *   {@link #gitDescribed()} in the future.
   * @return the maven compatible semver
   */
  public Semver getMaven() {
    return this.getMavenSnapshot();
  }

  /**
   * Semantic version based on git describe. Both Maven and Gradle Compatible.
   *
   * @implNote gradle compatability is somewhat assumed as gradle doesn't provide a valid way to
   *   unit test this assumption.
   *
   * @return semver
   */
  public Semver gitDescribed() {
    return new SemverBuilder(this.getGit()).build();
  }
}

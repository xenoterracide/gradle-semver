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
 * @implNote pre-release versions between branches which have the same git commit distance
 *         are not guaranteed to sort correctly and would do so only by coincidence.
 * @implNote Methods in this class are not lazy and invoke the
 *         {@link org.eclipse.jgit.lib.Repository}. All versions returned are Gradle safe as they
 *         are all valid semantic versions.
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
   * Gets git metatdata exstension.
   *
   * @return the extension for accessing git metdata
   * @implNote does not invoke {@link org.eclipse.jgit.lib.Repository}
   */
  GitMetadataExtension getGit() {
    return new GitMetadataExtension(this.git);
  }

  Try<Semver> coerced() {
    return this.getGit().describe().map(v -> null == v ? PRE_VERSION : v).map(Semver::coerce).filter(Objects::nonNull);
  }

  /**
   * Semantic version based on git describe. Both Maven and Gradle Compatible. Uses smarter,
   * lockable behavior than a {@code SNAPSHOT} release. You can release every commit without fear
   * that someone can't ensure they don't break their API.
   * <ul>
   *   <li>{@code 0.0.0-alpha.0.0}</li>
   *   <li>{@code 1.0.0-rc.1}</li>
   *   <li>{@code 1.0.0-rc.1.1+gabcdef10}</li>
   *   <li>{@code 1.0.0}</li>
   *   <li>{@code 1.0.1-alpha.0.1+gabcdef10}</li>
   *   <li>{@code 0.0.1-alpha.0.1+b-topic-foo.gabcdef10.dirty}</li>
   *   <li>{@code 0.0.1-alpha.0.1+gabcdef10.dirty}</li>
   * </ul>
   * <p>
   *     Given a git branch named {@code topic/foo}.<br>
   *     When the most recent commit to this branch is {@code gabcdef10} and it is 2 commits
   *     away from the most recent tag, which is {@code 1.2.0-rc.1}, and the workspace is not
   *     dirty.<br>
   *     Then the version would be {@code 1.2.0-rc.1.2+b-topic-foo.gabcdef10}.
   * </p>
   *
   * @return a semantic version based on git describe
   * @implNote when dealing with branches the last number before the build information
   *         should be based on the most recent ancenstor of the default branch. Meaning that in
   *         {@code 2.0.0-rc.2.1+gabcdef10} the {@code .1} will always come from the most recent
   *         ancenstor of the default branch, that branch is 1 commit away from the most recent
   *         tag. The {@code gabcdef10} would come from the actual branch you're on which could be
   *         any number of commits away from the last tag. This is done so that consumers don't
   *         accidentally pull in a branch version when working off our smarter snapshots.
   */
  public Semver getGitDescribed() {
    return new SemverBuilder(this.getGit()).build();
  }
}

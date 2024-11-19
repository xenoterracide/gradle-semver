// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.jgit.api.Git;
import org.jspecify.annotations.NonNull;
import org.semver4j.Semver;

/**
 * The Semver extension.
 *
 * @implNote pre-release versions between branches which have the same git commit distance are not
 *   guaranteed to sort correctly and would do so only by coincidence.
 * @implNote Methods in this class are not lazy and invoke the
 *   {@link org.eclipse.jgit.lib.Repository}. All versions returned are Gradle safe as they are all
 *   valid semantic versions.
 */
public class SemverExtension {

  private final Supplier<Optional<Git>> git;

  /**
   * Instantiates a new Semver extension.
   *
   * @param git
   *   {@link Supplier} of {@link Git}
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

  /**
   * Semantic version based on git describe. Both Maven and Gradle Compatible.
   * <ul>
   *   <li>{@code 0.0.0-alpha.0.0}</li>
   *   <li>{@code 0.0.1-alpha.0.1+abcdef10.dirty}</li>
   *   <li>{@code 1.0.0-rc.1}</li>
   *   <li>{@code 1.0.0-rc.1.1+abcdef10}</li>
   *   <li>{@code 1.0.0}</li>
   *   <li>{@code 1.0.1-alpha.0.1+abcdef10}</li>
   * </ul>
   *
   * @return semver
   * @implNote gradle compatability is somewhat assumed as gradle doesn't provide a valid way to
   *   unit test this assumption.
   */
  public Semver getGitDescribed() {
    return new SemverBuilder(this.getGit()).build();
  }
}

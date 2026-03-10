// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.GitExtension;
import com.xenoterracide.gradle.git.GitMetadata;
import com.xenoterracide.gradle.git.GitRemote;
import com.xenoterracide.gradle.git.GitStatus;
import com.xenoterracide.gradle.git.ProvidedFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.gradle.api.Project;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.jspecify.annotations.Nullable;
import org.semver4j.Semver;

/**
 * Extension for generating semantic versions from git metadata.
 * <ul>
 *  <li>{@see <a href="https://semver.org/">Semantic Versioning</a>}</li>
 *  <li>{@see <a href="https://git-scm.com/">Git</a>}</li>
 *  <li>{@see <a href="https://www.eclipse.org/jgit/">JGit</a>}</li>
 *  <li>{@link Semver}</li>
 * </ul>
 */
// CHECKSTYLE.OFF: FinalClass
public class SemverExtension {

  // CHECKSTYLE.ON: FinalClass

  private static final String UNKNOWN = "unknown";

  /**
   * Prefix for remote refs, copied from org.eclipse.jgit.lib.Constants.R_REMOTES.
   * Using our own constant avoids a direct dependency on JGit in this module.
   *
   * @see <a href="https://github.com/eclipse-jgit/jgit">JGit</a>
   */
  private static final String R_REMOTES = "refs/remotes/";

  private final Property<Semver> provider;
  private final Property<Boolean> checkDirty;
  private final Project project;

  /**
   * Instantiates a new Semver extension.
   *
   * @param project
   *   the project
   */
  private SemverExtension(Project project) {
    this.project = project;
    var pf = new ProvidedFactory(project);
    this.provider = pf.property(Semver.class);
    this.checkDirty = pf.propertyBoolean();
  }

  static SemverExtension forProject(Project project) {
    return new SemverExtension(project).build();
  }

  /**
   * Finds the origin remote from a list of remotes.
   *
   * @param remotes the list of remotes
   * @return optional of the origin remote
   */
  static Optional<GitRemote> findOrigin(List<GitRemote> remotes) {
    return remotes
      .stream()
      .filter(remote -> Objects.equals(remote.name(), "origin"))
      .findAny();
  }

  /**
   * Gets the HEAD branch name from a remote.
   *
   * @param origin the origin remote
   * @return the HEAD branch name, or null if not available
   */
  static @Nullable String getHeadBranchName(GitRemote origin) {
    var headBranchRef = origin.headBranchRefName();
    if (headBranchRef == null) {
      return null;
    }
    // Convert refs/remotes/origin/main -> main
    var prefix = R_REMOTES + origin.name() + "/";
    if (headBranchRef.startsWith(prefix)) {
      return headBranchRef.substring(prefix.length());
    }
    return null;
  }

  /**
   * Creates a provider that builds GitContext from GitExtension providers.
   *
   * @param gitExt the git extension
   * @return provider of GitContext
   */
  private Provider<GitContext> createGitContextProvider(GitExtension gitExt) {
    return gitExt.getProvider().map(gitMetadata -> this.buildGitContext(gitMetadata, gitExt));
  }

  /**
   * Builds a GitContext from GitMetadata and GitExtension.
   *
   * @param gitMetadata the git metadata
   * @param gitExt the git extension (for merge base calculations)
   * @return the git context
   */
  // CHECKSTYLE.OFF: MethodLength
  private GitContext buildGitContext(GitMetadata gitMetadata, GitExtension gitExt) {
    var remotes = gitMetadata.remotes();
    var originOpt = findOrigin(remotes);

    var currentBranch = gitMetadata.branch();
    var headBranch = originOpt.map(SemverExtension::getHeadBranchName).orElse(null);
    // When there's no HEAD branch configured (headBranch is null), treat current branch as HEAD
    var isHeadBranch = headBranch == null || Objects.equals(currentBranch, headBranch);

    // Calculate distance from merge base for topic branches
    var distanceFromMergeBase = calculateDistanceFromMergeBase(gitMetadata, gitExt, originOpt, isHeadBranch);

    var tag = gitMetadata.tag();
    var distanceFromTag = gitMetadata.distance();
    var isOnTagExact = tag != null && distanceFromTag == 0;

    // Get short SHA from git's unique abbreviation, or "unknown" if not available
    var shortSha = Optional.ofNullable(gitMetadata.uniqueShort()).orElse(UNKNOWN);
    var fullSha = Optional.ofNullable(gitMetadata.commit()).orElse(UNKNOWN);

    // Check if dirty (only if checkDirty is enabled)
    var isDirty = this.checkDirty.getOrElse(false) && gitMetadata.status() == GitStatus.DIRTY;

    // Shallow clone detection could be added here
    var isShallowClone = false;

    return GitContext.builder()
      .nearestTag(tag)
      .distanceFromTag(distanceFromTag)
      .isOnTagExact(isOnTagExact)
      .currentBranch(currentBranch)
      .headBranch(headBranch)
      .isHeadBranch(isHeadBranch)
      .distanceFromMergeBase(distanceFromMergeBase)
      .shortSha(shortSha)
      .fullSha(fullSha)
      .isDirty(isDirty)
      .isShallowClone(isShallowClone)
      .build();
  }

  // CHECKSTYLE.ON: MethodLength

  /**
   * Calculates the distance from merge base for topic branches.
   *
   * @param gitMetadata the git metadata
   * @param gitExt the git extension
   * @param originOpt optional of origin remote
   * @param isHeadBranch whether we're on the HEAD branch
   * @return distance from merge base
   */
  // CHECKSTYLE.OFF: ReturnCount
  private static long calculateDistanceFromMergeBase(
    GitMetadata gitMetadata,
    GitExtension gitExt,
    Optional<GitRemote> originOpt,
    boolean isHeadBranch
  ) {
    if (isHeadBranch || originOpt.isEmpty()) {
      // On HEAD branch or no origin: merge base distance equals tag distance
      return gitMetadata.distance();
    }

    // On topic branch: try to get distance from merge base
    var origin = originOpt.get();
    return gitExt.commonAncestorDistanceFor(origin).orElseGet(gitMetadata::distance);
  }

  // CHECKSTYLE.ON: ReturnCount

  SemverExtension build() {
    var gitExt = this.project.getExtensions().getByType(GitExtension.class);
    var projectName = this.project.getName();

    // Create GitContext provider and map it through the strategy machine
    var gitContextProvider = this.createGitContextProvider(gitExt);

    var semverProvider = gitContextProvider.map(ctx -> {
      var strategy = VersionStrategyFactory.determineStrategy(ctx);
      var version = strategy.calculate();
      Logging.getLogger(SemverExtension.class).info(
        "semver {} {} (strategy: {})",
        projectName,
        version,
        strategy.getClass().getSimpleName()
      );
      return version;
    });

    this.provider.set(semverProvider);
    this.provider.finalizeValueOnRead();
    this.provider.disallowChanges();
    return this;
  }

  /**
   * {@link Provider} of {@link Semver}. For a distance of 1 away from tag or your HEAD branch, but 40 commits on your
   * deviated branch.
   * {@code 0.1.1-alpha.0.1+git.1.3aae11e}. The longest example
   * {@code 0.1.1-alpha.0.1+branch.topic-foo.git.40.3aae11e.dirty}
   *
   * @return semver provider
   * @implSpec {@code
   *   <major>.<minor>.<patch>[-<preRelease.tag.headBranchDistance>][+branch.<branch>.]git.<distance>.<sha>[.dirty]]}
   * @implNote The value will not be recalculated more than once per project per build. It is suggested to only use on
   *   the root project.
   */
  public Provider<Semver> getProvider() {
    return this.provider;
  }

  /**
   * Dirty checking will cause your configuration cache to need to be changed every single file change.
   *
   * @return dirty check configuration property
   * @implNote The plugin defaults to false
   */
  public Property<Boolean> getCheckDirty() {
    return this.checkDirty;
  }

  /**
   * Returns a string representation of the object, which is retrieved from the provider's value.
   *
   * @return the string representation of the object provided by the {@code provider}
   */
  @Override
  public String toString() {
    return this.provider.getOrElse(Semver.ZERO).toString();
  }
}

// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later WITH Classpath-exception-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.GitExtension;
import com.xenoterracide.gradle.git.GitMetadata;
import com.xenoterracide.gradle.git.GitRemote;
import com.xenoterracide.gradle.git.GitStatus;
import com.xenoterracide.gradle.git.ProvidedFactory;
import com.xenoterracide.gradle.git.Provides;
import com.xenoterracide.gradle.semver.internal.GitContext;
import com.xenoterracide.gradle.semver.internal.VersionStateMachine;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.jgit.lib.Constants;
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
public class SemverExtension implements Provides<Semver> {

  // CHECKSTYLE.ON: FinalClass

  private static final String UNKNOWN = "unknown";

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
      .filter(remote -> Predicate.isEqual("origin").test(remote.name()))
      .findAny();
  }

  /**
   * Gets the HEAD branch name from a remote.
   *
   * @param origin the origin remote
   * @return the HEAD branch name, or null if not available
   */
  // CHECKSTYLE.OFF: ReturnCount
  static @Nullable String getHeadBranchName(GitRemote origin) {
    var headBranchRef = origin.headBranchRefName();
    if (headBranchRef == null) {
      return null;
    }
    // Convert refs/remotes/origin/main -> main
    var prefix = Constants.R_REMOTES + origin.name() + "/";
    if (headBranchRef.startsWith(prefix)) {
      return headBranchRef.substring(prefix.length());
    }
    return null;
  }

  // CHECKSTYLE.ON: ReturnCount

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
    var isHeadBranch = Objects.equals(currentBranch, headBranch);

    // Calculate distance from merge base for topic branches
    var distanceFromMergeBase = calculateDistanceFromMergeBase(gitMetadata, gitExt, originOpt, isHeadBranch);

    var tag = gitMetadata.tag();
    var distanceFromTag = gitMetadata.distance();
    var isOnTagExact = tag != null && distanceFromTag == 0;

    // Get short SHA from uniqueShort or derive from commit
    var shortSha = Optional.ofNullable(gitMetadata.uniqueShort()).orElse(UNKNOWN);
    var fullSha = Optional.ofNullable(gitMetadata.commit()).orElse(UNKNOWN);

    // Check if dirty (only if checkDirty is enabled)
    var isDirty = this.checkDirty.getOrElse(false) && gitMetadata.status() == GitStatus.DIRTY;

    // Shallow clone detection could be added here
    var isShallowClone = false;

    return new GitContext(
      tag,
      distanceFromTag,
      isOnTagExact,
      currentBranch,
      headBranch,
      isHeadBranch,
      distanceFromMergeBase,
      shortSha,
      fullSha,
      isDirty,
      isShallowClone
    );
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
    if (isHeadBranch) {
      // On HEAD branch: merge base distance equals tag distance
      return gitMetadata.distance();
    }

    if (originOpt.isEmpty()) {
      // No origin: can't calculate merge base
      return gitMetadata.distance();
    }

    // On topic branch: try to get distance from merge base
    var origin = originOpt.get();
    var mergeBaseDistanceOpt = gitExt.commonAncestorDistanceFor(origin);

    return mergeBaseDistanceOpt.orElse(gitMetadata.distance());
  }

  // CHECKSTYLE.ON: ReturnCount

  SemverExtension build() {
    var gitExt = this.project.getExtensions().getByType(GitExtension.class);
    var projectName = this.project.getName();

    // Create GitContext provider and map it through the state machine
    var gitContextProvider = this.createGitContextProvider(gitExt);

    var semverProvider = gitContextProvider.map(ctx -> {
      var version = VersionStateMachine.calculate(ctx);
      Logging.getLogger(SemverExtension.class).info(
        "semver {} {} (state: {})",
        projectName,
        version,
        VersionStateMachine.determineState(ctx).getClass().getSimpleName()
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
  @Override
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

// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.GitExtension;
import com.xenoterracide.gradle.git.GitRemoteForGradle;
import com.xenoterracide.gradle.git.ProvidedFactory;
import com.xenoterracide.gradle.git.Provides;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.logging.Logger;
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

  private final Logger log = Logging.getLogger(this.getClass());
  private final Property<Semver> provider;
  private final Property<Boolean> checkDirty;
  private final Property<String> remote;
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
    this.remote = pf.propertyString();
  }

  static SemverExtension forProject(Project project) {
    return new SemverExtension(project).build();
  }

  static Optional<GitRemoteForGradle> getOrigin(List<GitRemoteForGradle> remotes) {
    return remotes
      .stream()
      .filter(remote -> Objects.equals(remote.getName(), "origin"))
      .filter(remote -> remote.getHeadBranch().isPresent())
      .findAny();
  }

  static Provider<String> getBranch(GitExtension gitExt) {
    return gitExt
      .getRemotes()
      .map(remotes -> getOrigin(remotes).map(remote -> remote.getHeadBranch().get()))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .zip(gitExt.getBranch(), (remoteBranch, localBranch) ->
        Objects.equals(remoteBranch, localBranch) ? null : localBranch
      );
  }

  static Function<GitRemoteForGradle, @Nullable Long> commonAncestorDistanceFor(GitExtension gitExt) {
    return remote -> gitExt.commonAncestorDistanceFor(remote).orElse(null);
  }

  static Provider<Long> getDistance(GitExtension gitExt) {
    return gitExt
      .getRemotes()
      .map(SemverExtension::getOrigin)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .map(commonAncestorDistanceFor(gitExt)::apply)
      .orElse(gitExt.getDistance());
  }

  Transformer<Semver, Semver> configureBuilder(GitExtension gitExt) {
    return semver -> {
      return new SemverBuilder(semver)
        .withDirtyOut(this.getCheckDirty().getOrElse(false))
        .withPreReleaseDistance(getDistance(gitExt).getOrElse(0L))
        .withBuildDistance(gitExt.getDistance().getOrElse(0L))
        .withGitStatus(gitExt.getStatus().get())
        .withUniqueShort(gitExt.getUniqueShort().getOrNull())
        .withBranch(getBranch(gitExt).getOrNull())
        .build();
    };
  }

  SemverExtension build() {
    var gitExt = this.project.getExtensions().getByType(GitExtension.class);

    var semverProvider = gitExt
      .getTag()
      .map(tag -> Objects.requireNonNull(Semver.parse(tag)))
      .orElse(Semver.ZERO)
      .map(this.configureBuilder(gitExt))
      .map(semver -> {
        this.log.info("semver {} {}", this.project.getName(), semver);
        return semver;
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
   * Remote to use for head branch configuration.
   *
   * @return remote configuration property
   * @implNote The plugin defaults to "origin"
   */
  @Incubating
  public Property<String> getRemote() {
    return this.remote;
  }
}

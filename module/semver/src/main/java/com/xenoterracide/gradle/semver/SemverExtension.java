// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.BranchOutput;
import com.xenoterracide.gradle.git.DistanceCalculator;
import com.xenoterracide.gradle.git.GitExtension;
import com.xenoterracide.gradle.git.GitService;
import com.xenoterracide.gradle.git.ProvidedFactory;
import com.xenoterracide.gradle.git.Provides;
import com.xenoterracide.gradle.git.RemoteForHeadBranch;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildServiceRegistration;
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
public class SemverExtension implements Provides<Semver> {

  private final Logger log = Logging.getLogger(this.getClass());
  private final Property<Semver> provider;
  private final Property<Boolean> checkDirty;
  private final Property<BranchOutput> branchOutput;
  private final Property<RemoteForHeadBranch> remoteForHeadBranchConfig;
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
    this.branchOutput = pf.property(BranchOutput.class);
    this.provider = pf.property(Semver.class);
    this.remoteForHeadBranchConfig = pf.property(RemoteForHeadBranch.class);
    this.checkDirty = pf.propertyBoolean();
    this.remote = pf.propertyString();
  }

  static SemverExtension forProject(Project project) {
    return new SemverExtension(project).build();
  }

  SemverExtension build() {
    var gitExt = project.getExtensions().getByType(GitExtension.class);

    var semverProvider =
      this.project.getGradle()
        .getSharedServices()
        .getRegistrations()
        .named(GitService.class.getCanonicalName())
        .flatMap(BuildServiceRegistration::getService)
        .map(GitService.class::cast)
        .flatMap(GitService::provider)
        .map(git -> {
          var dc = new DistanceCalculator(() -> git);
          var semver = gitExt.getTag().map(Semver::parse).getOrElse(Semver.ZERO);

          return new SemverBuilder(dc, semver)
            .withDirtyOut(this.getCheckDirty().getOrElse(false))
            .withBranchOutput(this.getBranchOutput().getOrElse(BranchOutput.NON_HEAD_BRANCH_OR_THROW))
            .withRemoteForHeadBranchConfig(
              this.getRemoteForHeadBranchConfig().getOrElse(RemoteForHeadBranch.CONFIGURED_ORIGIN_OR_THROW)
            )
            .build();
        })
        .orElse(Semver.ZERO)
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
   * {@link Provider} of {@link Semver}. For a distance of 1 away from tag or your HEAD branch
   * {@code 0.1.1-alpha.0.1+.g3aae11e}. The longest example {@code 0.1.1-alpha.0.1+btopic-foo.g3aae11e.dirty}
   *
   * @return semver provider
   * @implSpec {@code <major>.<minor>.<patch>[-<preRelease.tag.distance>][+[b<branch>.]g<sha>[.dirty]]}
   * @implNote The value will not be recalculated more than once per project per build. It is suggested to only use on
   *   the root project. In the future this may be a single global calculation.
   */
  @Override
  public Provider<Semver> provider() {
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
   * Branch output configuration.
   *
   * @return branch output configuration property
   * @implNote The plugin defaults to {@link BranchOutput#NON_HEAD_BRANCH_OR_THROW}
   */
  @Incubating
  public Property<BranchOutput> getBranchOutput() {
    return this.branchOutput;
  }

  /**
   * Remote for head branch configuration. These only matter if more than one remote is configured otherwise the remote
   * found is used.
   *
   * @return remote for head branch configuration property
   * @implNote The plugin defaults to {@link RemoteForHeadBranch#CONFIGURED_ORIGIN_OR_FIRST}
   */
  @Incubating
  public Property<RemoteForHeadBranch> getRemoteForHeadBranchConfig() {
    return this.remoteForHeadBranchConfig;
  }

  public Property<String> getRemote() {
    return this.remote;
  }
}

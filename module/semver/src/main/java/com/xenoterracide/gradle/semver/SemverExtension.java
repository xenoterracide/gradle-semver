// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.semver.internal.DistanceCalculator;
import com.xenoterracide.gradle.semver.internal.ProvidedFactory;
import com.xenoterracide.gradle.semver.internal.TryGit;
import java.util.function.Supplier;
import org.eclipse.jgit.api.Git;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.semver4j.Semver;

/**
 * Extension for generating semantic versions from git metadata.
 * <ul>
 *  <li>{@see <a href="https://semver.org/">Semantic Versioning</a>}</li>
 *  <li>{@see <a href="https://git-scm.com/">Git</a>}</li>
 *  <li>{@link Semver}</li>
 *  <li>{@link Git}</li>
 * </ul>
 */
public class SemverExtension {

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
  protected SemverExtension(Project project) {
    this.project = project;
    var pf = new ProvidedFactory(project);
    this.branchOutput = pf.property(BranchOutput.class);
    this.provider = pf.property(Semver.class);
    this.remoteForHeadBranchConfig = pf.property(RemoteForHeadBranch.class);
    this.checkDirty = pf.propertyBoolean();
    this.remote = pf.propertyString();
  }

  SemverExtension init(Supplier<TryGit> tryGit) {
    var semverProvider =
      this.project.provider(() -> {
          var gm = this.project.getExtensions().getByType(GitMetadataExtension.class);
          var semver = new SemverBuilder(new GitMetadataExtensionAdapter(gm), new DistanceCalculator(tryGit))
            .withDirtyOut(this.getCheckDirty().getOrElse(false))
            .withBranchOutput(this.getBranchOutput().getOrElse(BranchOutput.NON_HEAD_BRANCH_OR_THROW))
            .withRemoteForHeadBranchConfig(
              this.getRemoteForHeadBranchConfig().getOrElse(RemoteForHeadBranch.CONFIGURED_ORIGIN_OR_THROW)
            )
            .build();
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

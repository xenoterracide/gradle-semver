// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static com.xenoterracide.gradle.semver.internal.GradleTools.finalOnRead;

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
 *  <li>{@link org.eclipse.jgit.api.Git}</li>
 * </ul>
 */
public class SemverExtension {

  private final Logger log = Logging.getLogger(this.getClass());
  private final Property<Semver> provider;
  private final Property<Boolean> checkDirty;

  protected SemverExtension(GitMetadata gm, Project project) {
    var semverProvider = project
      .getProviders()
      .provider(() -> {
        var semver = new SemverBuilder(gm).withDirtyOut(this.getCheckDirty().getOrElse(false)).build();
        this.log.info("semver {} {}", project.getName(), semver);
        return semver;
      });
    var of = project.getObjects();
    var semverProperty = finalOnRead(of.property(Semver.class));
    semverProperty.set(semverProvider);
    semverProperty.disallowChanges();
    this.provider = semverProperty;
    this.checkDirty = finalOnRead(of.property(Boolean.class));
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
}

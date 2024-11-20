// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import static com.xenoterracide.gradle.semver.internal.GradleTools.finalOnRead;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.semver4j.Semver;

public class SemverExtension {

  private final Provider<Semver> provider;
  private final Property<Boolean> checkDirty;

  protected SemverExtension(GitMetadata gm, Project project) {
    var semverProvider = project
      .getProviders()
      .provider(() -> {
        var semver = new SemverBuilder(gm).withDirtyOut(this.getCheckDirty().getOrElse(false)).build();
        project.getLogger().quiet("{} semver: {}", project.getName(), semver);
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
   * {@link Provider} of {@link Semver}.
   *
   * @return semver provider
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

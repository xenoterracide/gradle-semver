// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver;

import com.xenoterracide.gradle.git.GitExtension;
import com.xenoterracide.gradle.git.ProvidedFactory;
import com.xenoterracide.gradle.git.Provides;
import java.util.Objects;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
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

  Transformer<Semver, Semver> configureBuilder(GitExtension gitExt) {
    return semver -> {
      return new SemverBuilder(semver)
        .withDirtyOut(this.getCheckDirty().getOrElse(false))
        .withDistance(gitExt.getDistance().get())
        .withGitStatus(gitExt.getStatus().get())
        .withUniqueShort(gitExt.getUniqueShort().getOrNull())
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
   * {@link Provider} of {@link Semver}. For a distance of 1 away from tag or your HEAD branch
   * {@code 0.1.1-alpha.0.1+.g3aae11e}. The longest example {@code 0.1.1-alpha.0.1+btopic-foo.g3aae11e.dirty}
   *
   * @return semver provider
   * @implSpec {@code <major>.<minor>.<patch>[-<preRelease.tag.distance>][+[b<branch>.]g<sha>[.dirty]]}
   * @implNote The value will not be recalculated more than once per project per build. It is suggested to only use on
   *   the root project. In the future this may be a single global calculation.
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

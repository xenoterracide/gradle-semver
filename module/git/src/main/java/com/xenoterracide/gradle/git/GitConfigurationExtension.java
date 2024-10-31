// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSourceParameters;

/**
 * Information that may need explicit configuration.
 */
public interface GitConfigurationExtension extends ValueSourceParameters {
  /**
   * The `HEAD Branch` is also known as the default branch and is defined by your remote. To see this for the
   * {@code origin} remote you can run {@code git remote show origin} and you will see a field called
   * {@code HEAD branch}.
   *
   * @return the branch name.
   */
  Property<String> getHeadBranch();

  /**
   * this should be the remote that is the source of truth. The lookup algorithm is as follows:
   * <ol>
   *   <li>The explicit value set here.</li>
   *   <li>
   *   Gradle {@code git.sourceRemote}
   *    * {@see <a href=
   *    * "https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties"
   *    * >project property</a>}.
   *    </li>
   *    <li>The first remote returned by jgit.</li>
   *    <li>null</li>
   * </ol>
   *  If that is not set then it will return the first remote that is found.
   *
   * @implNote if git does not have any remotes configured then any property derived from this will return null
   *   irregardles.
   */
  Property<String> getSourceRemote();
}

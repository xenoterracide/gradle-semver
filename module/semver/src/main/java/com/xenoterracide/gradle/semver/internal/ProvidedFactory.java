// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import java.util.List;
import java.util.concurrent.Callable;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

/**
 * Property, e.g. {@link ObjectFactory} and {@link ProviderFactory} wrapper.
 */
public final class ProvidedFactory {

  private final ObjectFactory objectFactory;
  private final ProviderFactory providerFactory;

  /**
   * Instantiates a new Provided factory from a {@link Project}.
   *
   * @param project
   *   the project
   */
  public ProvidedFactory(Project project) {
    this(project.getObjects(), project.getProviders());
  }

  private ProvidedFactory(ObjectFactory objectFactory, ProviderFactory providerFactory) {
    this.objectFactory = objectFactory;
    this.providerFactory = providerFactory;
  }

  /**
   * Shortcut to {@link #provided(Callable, Class)} for {@link String}.
   *
   * @param callable
   *   function to provide value
   * @return provider
   * @see #provided(Callable, Class)
   */
  public Provider<String> providedString(Callable<String> callable) {
    return this.provided(callable, String.class);
  }

  /**
   * Shortcut to {@link #provided(Callable, Class)} for {@link Integer}.
   *
   * @param callable
   *   function to provide value
   * @return provider
   * @see #provided(Callable, Class)
   */
  public Provider<Integer> providedInt(Callable<Integer> callable) {
    return this.provided(callable, Integer.class);
  }

  /**
   * Shortcut to {@link #provided(Callable, Class)} for {@link Long}.
   *
   * @param callable
   *   function to provide value
   * @return provider
   * @see #provided(Callable, Class)
   */
  public Provider<Long> providedLong(Callable<Long> callable) {
    return this.provided(callable, Long.class);
  }

  /**
   * Provides functionality similar to {@link #provided(Callable, Class)} for {@link List}.
   *
   * @param <E>
   *   element type for list
   * @param callable
   *   function to provide value
   * @param type
   *   element class
   * @return string provider
   * @see #provided(Callable, Class)
   */
  public <E> Provider<List<E>> providedList(Callable<List<E>> callable, Class<E> type) {
    var prop = this.objectFactory.listProperty(type);
    prop.set(this.providerFactory.provider(callable));
    prop.finalizeValueOnRead();
    prop.disallowChanges();
    return prop;
  }

  /**
   * Create an {@link Provider} from a {@link Callable} and {@link Class} ensuring the callback is not called repeatedly
   * during a build.
   *
   * @param <T>
   *   type the provider returns
   * @param callable
   *   function to provide value
   * @param type
   *   the class for the type that the provider returns
   * @return string provider
   * @implNote currently this returns a {@link Property} so that the property should only be calculated once per
   *   instance of it. This implementation could change in the future to ensure only once per build. A {@link Property}
   *   based solution should not be assumed, but currently use of {@link Property#finalizeValueOnRead()} and
   *   {@link Property#disallowChanges()} to ensure they are immutable and only created as a sort of cached
   *   {@link Provider}.
   */
  public <T> Provider<T> provided(Callable<T> callable, Class<T> type) {
    var prop = this.objectFactory.property(type);
    prop.set(this.providerFactory.provider(callable));
    prop.finalizeValueOnRead();
    prop.disallowChanges();
    return prop;
  }

  /**
   * Shortcut to {@link #property(Class)} for {@link Boolean}.
   *
   * @return the new property
   * @see #property(Class)
   */
  public Property<Boolean> propertyBoolean() {
    return this.property(Boolean.class);
  }

  /**
   * Shortcut to {@link #property(Class)} for {@link String}.
   *
   * @return the new property
   * @see #property(Class)
   */
  public Property<String> propertyString() {
    return this.property(String.class);
  }

  /**
   * Creates a {@link Property} of a given type that is {@link Property#finalizeValueOnRead()}.
   *
   * @param type
   *   class type of property
   * @param <T>
   *   type of property
   * @return the new property
   * @see ObjectFactory#property(Class)
   */
  public <T> Property<T> property(Class<T> type) {
    var prop = this.objectFactory.property(type);
    prop.finalizeValueOnRead();
    return prop;
  }
}

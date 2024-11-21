// © Copyright 2024 Caleb Cushing
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

  public ProvidedFactory(Project project) {
    this(project.getObjects(), project.getProviders());
  }

  ProvidedFactory(ObjectFactory objectFactory, ProviderFactory providerFactory) {
    this.objectFactory = objectFactory;
    this.providerFactory = providerFactory;
  }

  public Provider<String> providedString(Callable<String> callable) {
    return this.provided(callable, String.class);
  }

  public Provider<Integer> providedInt(Callable<Integer> callable) {
    return this.provided(callable, Integer.class);
  }

  public <E> Provider<List<E>> providedList(Callable<List<E>> callable, Class<E> type) {
    var prop = this.objectFactory.listProperty(type);
    prop.set(providerFactory.provider(callable));
    prop.disallowChanges();
    prop.disallowUnsafeRead();
    return prop;
  }

  public <T> Provider<T> provided(Callable<T> callable, Class<T> type) {
    var prop = this.objectFactory.property(type);
    prop.set(providerFactory.provider(callable));
    prop.disallowChanges();
    prop.disallowUnsafeRead();
    return prop;
  }

  public Property<Boolean> propertyBoolean() {
    return this.property(Boolean.class);
  }

  public <T> Property<T> property(Class<T> type) {
    var prop = this.objectFactory.property(type);
    prop.disallowUnsafeRead();
    return prop;
  }
}

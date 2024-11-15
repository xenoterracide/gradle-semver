// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import com.xenoterracide.gradle.git.internal.GitMetadata;
import com.xenoterracide.gradle.git.internal.GradleTools;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.jspecify.annotations.Nullable;

class GitMetadataExtensionImpl implements GitMetadataExtension {

  private final ProviderFactory providerFactory;
  private final ObjectFactory objectFactory;
  private final GitMetadata git;

  GitMetadataExtensionImpl(ProviderFactory providerFactory, ObjectFactory objectFactory, GitMetadata git) {
    this.providerFactory = providerFactory;
    this.objectFactory = objectFactory;
    this.git = git;
  }

  @Override
  public Provider<Map<String, Provider<String>>> getRemoteHeadBranches() {
    var prop = objectFactory.mapProperty(String.class, Provider.class);
    prop.set(
      this.providerFactory.provider(() -> {
          return this.git.remotes()
            .stream()
            .collect(
              Collectors.toMap(Function.identity(), remote -> {
                return this.providerFactory.of(HeadBranchValueSource.class, c -> {
                    c.parameters(p -> p.getRemote().set(remote));
                  });
              })
            );
        })
    );
    return GradleTools.finalizeOnRead(prop);
  }

  @Override
  public Provider<String> getUniqueShort() {
    return this.provideProperty(this.git::uniqueShort, String.class);
  }

  @Override
  public Provider<String> getLatestTag() {
    return this.provideProperty(this.git::tag, String.class);
  }

  @Override
  public Provider<Integer> getCommitDistance() {
    return this.provideProperty(this.git::distance, Integer.class);
  }

  @Override
  public Provider<Integer> getCommitDistanceOfAncestorInHeadBranch() {
    var map = this.getRemoteHeadBranches().get();
    map.get("origin");
    return this.provideProperty(this.git::distance, Integer.class);
  }

  @Override
  public Provider<GitStatus> getStatus() {
    return this.provideProperty(this.git::status, GitStatus.class);
  }

  private <T> Provider<T> provideProperty(Callable<@Nullable T> provider, Class<T> type) {
    var prop = this.objectFactory.property(type);
    prop.set(this.providerFactory.provider(provider));
    return GradleTools.finalizeOnRead(prop);
  }
}

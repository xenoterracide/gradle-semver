// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import io.vavr.control.Try;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.Git;

public abstract class RemotesValueSource
  implements GitValueSource<List<RemotesValueSource.Remote>, RemotesValueSourceParameters> {

  @Override
  public List<Remote> getValue(Git git) {
    return Optional.of(git)
      .map(Git::remoteList)
      .map(cmd -> Try.of(cmd::call))
      .map(t -> t.onFailure(e -> log.warn("Failed to get remotes", e)))
      .map(Try::get)
      .stream()
      .flatMap(Collection::stream)
      .map(r -> new Remote(r.getName()))
      .collect(Collectors.toList());
  }

  public static class Remote implements Serializable {

    private static final long serialVersionUID = 1L;

    public final String name;

    public Remote(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}

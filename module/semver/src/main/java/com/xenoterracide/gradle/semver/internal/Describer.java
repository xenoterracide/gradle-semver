// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import com.google.common.base.Splitter;
import io.vavr.CheckedFunction1;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

public class Describer implements CheckedFunction1<Git, Describer.Described> {

  private static final long serialVersionUID = 1L;
  private static final Splitter DESCRIBE_SPLITTER = Splitter.on('-');
  private static final String VERSION_GLOB = "v[0-9]*.[0-9]*.[0-9]*";

  private final ObjectId oid;

  Describer(ObjectId oid) {
    this.oid = oid;
  }

  static CheckedFunction1<Git, Describer.Described> describe(ObjectId oid) {
    return new Describer(oid);
  }

  @Override
  public @Nullable Described apply(Git git) throws InvalidPatternException, IOException, GitAPIException {
    var cmd = git.describe().setMatch(VERSION_GLOB).setLong(true).setTags(true);
    if (oid != null) cmd.setTarget(oid);
    var desc = cmd.call();
    return desc != null ? new Described(DESCRIBE_SPLITTER.splitToList(desc)) : null;
  }

  public static class Described {

    private final List<String> parts;

    Described(List<String> parts) {
      this.parts = parts;
    }

    public @Nullable ObjectId commit() {
      return parts.size() > 1 ? ObjectId.fromString(parts.get(parts.size() - 1)) : null;
    }

    public long distance() {
      return parts.size() > 2 ? Long.parseLong(parts.get(parts.size() - 2)) : 0;
    }

    public @Nullable String tag() {
      return !parts.isEmpty() ? parts.get(0) : null;
    }
  }
}

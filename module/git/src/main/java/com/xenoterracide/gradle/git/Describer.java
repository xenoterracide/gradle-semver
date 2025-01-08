// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import com.google.common.base.Splitter;
import io.vavr.CheckedFunction1;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

class Describer implements CheckedFunction1<Git, Describer.Described> {

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
    var cmd = git.describe().setMatch(VERSION_GLOB).setLong(true).setTags(true).setTarget(this.oid);
    var desc = cmd.call();
    return desc != null ? new Described(DESCRIBE_SPLITTER.splitToList(desc)) : null;
  }

  static class Described {

    private final List<String> parts;

    Described(List<String> parts) {
      this.parts = parts;
    }

    long distance() {
      return this.parts.size() > 2 ? Long.parseLong(this.parts.get(this.parts.size() - 2)) : 0;
    }
  }
}

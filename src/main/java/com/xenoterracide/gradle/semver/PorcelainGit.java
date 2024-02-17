// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.
package com.xenoterracide.gradle.semver;

import com.google.errorprone.annotations.Var;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.Repository;

class PorcelainGit {

  private static final String VERSION_PREFIX = "v";
  private static final String TAG_PREFIX = "refs/tags/";
  private static final String VERSION_GLOB = VERSION_PREFIX + "[0-9]*.[0-9]*.[0-9]*";

  private final Git git;

  PorcelainGit(@NonNull Git git) {
    this.git = Objects.requireNonNull(git);
  }

  @Nullable
  private static <T> T getLast(@NonNull List<T> list) {
    return !Objects.requireNonNull(list).isEmpty() ? list.get(list.size() - 1) : null;
  }

  @Nullable
  Ref findMostRecentTag() throws IOException {
    return getLast(getDb().getRefsByPrefix(TAG_PREFIX + VERSION_PREFIX));
  }

  private Repository getRepo() {
    return git.getRepository();
  }

  long countCommits(AnyObjectId a, AnyObjectId b)
    throws IncorrectObjectTypeException, MissingObjectException, GitAPIException {
    return StreamSupport.stream(git.log().addRange(a, b).call().spliterator(), true).count();
  }

  String describe() {
    try {
      @Var
      String version = git.describe().setMatch(VERSION_GLOB).call();
      if (version == null) {
        Ref mostRecentTag = findMostRecentTag();
        if (mostRecentTag != null) {
          String vtag = StringUtils.removeStart(mostRecentTag.getName(), TAG_PREFIX);
          ObjectId head = getRepo().resolve("HEAD");
          long commitCount = countCommits(mostRecentTag.getPeeledObjectId(), head.toObjectId());
          version = String.join("-", vtag, String.valueOf(commitCount), 'g' + head.name().substring(0, 7), "SNAPSHOT");
        }
      }
      return version;
    } catch (IOException | GitAPIException | InvalidPatternException e) {
      throw new RuntimeException(e);
    }
  }

  private RefDatabase getDb() {
    return getRepo().getRefDatabase();
  }
}

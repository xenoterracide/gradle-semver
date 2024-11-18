// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.internal;

import com.google.common.base.Splitter;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import java.io.File;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.SystemReader;
import org.jspecify.annotations.Nullable;

public final class GitTools {

  private static final SystemReader READER = new SystemReader.Delegate(SystemReader.getInstance()) {
    @Override
    public String getenv(String variable) {
      if ("PATH".equals(variable)) {
        return "";
      } else {
        return super.getenv(variable);
      }
    }
  };

  private GitTools() {}

  public static CheckedFunction1<Git, Ref> getHeadBranch(Supplier<String> remote) {
    return git -> Objects.requireNonNull(git.lsRemote().setRemote(remote.get()).callAsMap().get(Constants.HEAD));
  }

  public static Try.WithResources1<Git> openGit(Supplier<File> projectDir) {
    SystemReader.setInstance(READER);
    return Try.withResources(() -> {
      var gitDir = new FileRepositoryBuilder()
        .readEnvironment()
        .setMustExist(true)
        .findGitDir(projectDir.get())
        .getGitDir();
      return Git.open(gitDir);
    });
  }

  static Stream<String> splitOn(char delimeter, String value) {
    return StreamSupport.stream(Splitter.on(delimeter).split(value).spliterator(), false);
  }

  /**
   * Parse the output of {@code git remote show} to get the {@code HEAD branch).
   * <p>
   *
   * @param remoteShow
   * @return the HEAD branch or null if not found
   */
  public static @Nullable String parseHeadBranch(String remoteShow) {
    var headBranch = "  HEAD Branch";
    return splitOn('\n', remoteShow)
      .filter(line -> line.startsWith(headBranch))
      .flatMap(line -> splitOn(':', line))
      .filter(value -> !value.startsWith(headBranch))
      .map(String::trim)
      .findAny()
      .orElse(null);
  }
}

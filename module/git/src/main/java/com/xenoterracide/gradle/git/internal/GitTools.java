// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.internal;

import com.google.common.base.Splitter;
import io.vavr.control.Try;
import java.io.File;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.jgit.api.Git;
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

  static Try.WithResources1<Git> openGit(File projectDir) {
    SystemReader.setInstance(READER);
    return Try.withResources(() -> {
      var gitDir = new FileRepositoryBuilder().readEnvironment().setMustExist(true).findGitDir(projectDir).getGitDir();
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

  // preventJGitFromCallingExecutables is copied from
  // https://github.com/diffplug/spotless/blob/224f8f96df3ad42cac81064a0461e6d4ee91dcaf/plugin-gradle/src/main/java/com/diffplug/gradle/spotless/GitRatchetGradle.java#L35
  // SPDX-License-Identifier: Apache-2.0
  // Copyright 2020-2023 DiffPlug
  static void preventJGitFromCallingExecutables() {}
}

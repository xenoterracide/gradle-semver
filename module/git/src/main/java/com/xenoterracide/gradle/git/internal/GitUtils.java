// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.internal;

import com.google.common.base.Splitter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jspecify.annotations.Nullable;

public class GitUtils {

  static Stream<String> splitOn(char delimeter, String value) {
    return StreamSupport.stream(Splitter.on(delimeter).split(value).spliterator(), false);
  }

  /**
   * Takes an {@link ByteArrayOutputStream} and returns the HEAD branch using {@link #parseHeadBranch(String)}. Likely
   * to be switched to a standard {@link java.io.ByteArrayOutputStream} when java 17 is the minimum version.
   *
   * @param baos
   * @return
   */
  public static @Nullable String getHeadBranch(ByteArrayOutputStream baos) {
    return parseHeadBranch(baos.toString(StandardCharsets.UTF_8));
  }

  /**
   * Parse the output of {@code git remote show} to get the {@code HEAD branch).
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

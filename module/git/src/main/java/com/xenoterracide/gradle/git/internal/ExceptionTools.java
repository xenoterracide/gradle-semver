// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git.internal;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * The Exception Utilities.
 */
public final class ExceptionTools {

  private ExceptionTools() {}

  /**
   * Rethrow a {@link Throwable} as a {@link RuntimeException}.
   *
   * @param e exception to rethrow
   * @return never actually returns
   */
  public static RuntimeException rethrow(Throwable e) {
    throw toRuntime(e);
  }

  /**
   * Converts a checked exception to a {@link RuntimeException}.
   * {@snippet :
   *   try {
   *     throw new IOException();
   *   } catch (Exception e) {
   *     throw ExceptionTools.toRuntime(e);
   *   }
   *   // Try is from vavr.io
   *   Try.of(() -> { throw new IOException(); })
   *     .getOrElseThrow(ExceptionTools::toRuntime);
   *}
   *
   * @param e the exception to convert.
   * @return {@link RuntimeException} the converted exception.
   * @implNote <ul>
   *   <li>{@link IOException} to {@link UncheckedIOException}.</li>
   *   <li>{@link RuntimeException} is returned as is.</li>
   *   <li>Other exceptions are wrapped in a {@link RuntimeException}.</li>
   *   </ul>
   * @see <a href="https://docs.vavr.io/#_try">vavr.io Try</a>
   */
  // CHECKSTYLE.OFF: ReturnCount
  public static RuntimeException toRuntime(Throwable e) {
    if (e instanceof IOException) {
      return new UncheckedIOException((IOException) e);
    }
    if (e instanceof RuntimeException) {
      return (RuntimeException) e;
    }
    return new RuntimeException(e);
  }
  // CHECKSTYLE.ON
}

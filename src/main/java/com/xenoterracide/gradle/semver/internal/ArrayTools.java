// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

/**
 * Utility class for working with arrays.
 */
public final class ArrayTools {

  private ArrayTools() {}

  /**
   * Slice byte [].
   *
   * @param array the array
   * @param start start index
   * @param end   end index
   * @return the new byte array
   */
  public static byte[] slice(byte[] array, int start, int end) {
    if (start < 0) {
      throw new ArrayIndexOutOfBoundsException("start < 0");
    }
    if (end > array.length) {
      throw new ArrayIndexOutOfBoundsException("end > array.length");
    }
    if (start > end) {
      throw new ArrayIndexOutOfBoundsException("start > end");
    }
    int length = end - start;
    byte[] result = new byte[length];
    System.arraycopy(array, start, result, 0, length);
    return result;
  }
}

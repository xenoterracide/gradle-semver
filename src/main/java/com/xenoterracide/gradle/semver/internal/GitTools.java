// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.internal;

import java.nio.ByteBuffer;
import java.util.Formatter;
import org.eclipse.jgit.lib.ObjectId;

/**
 * Utility class for working with JGit.
 */
public final class GitTools {

  private GitTools() {}

  public static String toOctal(ObjectId objectId, int size) {
    var sha = ByteBuffer.allocate(20);
    objectId.copyRawTo(sha);
    var oct = new Formatter();
    for (var byt : ArrayTools.slice(sha.array(), 0, size)) {
      oct.format("%03o", byt);
    }
    return oct.toString();
  }
}

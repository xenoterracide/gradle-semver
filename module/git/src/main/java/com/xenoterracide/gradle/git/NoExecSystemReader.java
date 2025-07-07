// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import org.eclipse.jgit.util.SystemReader;
import org.eclipse.jgit.util.SystemReader.Delegate;

class NoExecSystemReader extends Delegate {

  static {
    getCreateAndSet();
  }

  NoExecSystemReader() {
    super(SystemReader.getInstance());
  }

  static SystemReader getOrNew() {
    if (SystemReader.getInstance() instanceof NoExecSystemReader) return SystemReader.getInstance();
    return new NoExecSystemReader();
  }

  static SystemReader getCreateAndSet() {
    var reader = getOrNew();
    if (reader != SystemReader.getInstance()) {
      SystemReader.setInstance(reader);
    }
    return reader;
  }

  @Override
  public String getenv(String variable) {
    if ("PATH".equals(variable)) {
      return "";
    } else {
      return super.getenv(variable);
    }
  }
}

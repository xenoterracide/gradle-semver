// SPDX-License-Identifier: Apache-2.0
// © Copyright 2024 Caleb Cushing. All rights reserved.

package com.xenoterracide.gradle.semver.internal;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;

class DelegatingSystemReader extends SystemReader {

  private final SystemReader reader;

  DelegatingSystemReader(SystemReader reader) {
    this.reader = reader;
  }

  @Override
  public String getHostname() {
    return reader.getHostname();
  }

  @Override
  public String getenv(String variable) {
    return reader.getenv(variable);
  }

  @Override
  public String getProperty(String key) {
    return reader.getProperty(key);
  }

  @Override
  public FileBasedConfig openUserConfig(Config parent, FS fs) {
    return reader.openUserConfig(parent, fs);
  }

  @Override
  public FileBasedConfig openSystemConfig(Config parent, FS fs) {
    return reader.openSystemConfig(parent, fs);
  }

  @Override
  public FileBasedConfig openJGitConfig(Config parent, FS fs) {
    return reader.openJGitConfig(parent, fs);
  }

  @Override
  public long getCurrentTime() {
    return reader.getCurrentTime();
  }

  @Override
  public int getTimezone(long when) {
    return reader.getTimezone(when);
  }
}

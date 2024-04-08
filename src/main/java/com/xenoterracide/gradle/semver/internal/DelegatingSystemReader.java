// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

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
    return this.reader.getHostname();
  }

  @Override
  public String getenv(String variable) {
    return this.reader.getenv(variable);
  }

  @Override
  public String getProperty(String key) {
    return this.reader.getProperty(key);
  }

  @Override
  public FileBasedConfig openUserConfig(Config parent, FS fs) {
    return this.reader.openUserConfig(parent, fs);
  }

  @Override
  public FileBasedConfig openSystemConfig(Config parent, FS fs) {
    return this.reader.openSystemConfig(parent, fs);
  }

  @Override
  public FileBasedConfig openJGitConfig(Config parent, FS fs) {
    return this.reader.openJGitConfig(parent, fs);
  }

  @Override
  public long getCurrentTime() {
    return this.reader.getCurrentTime();
  }

  @Override
  public int getTimezone(long when) {
    return this.reader.getTimezone(when);
  }
}

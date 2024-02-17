// SPDX-License-Identifier: Apache-2.0
// Copyright © 2018-2024 Caleb Cushing.
package com.xenoterracide.gradle.semver;

import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.SystemReader;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemVerPlugin implements Plugin<Project> {
  static {
    preventJGitFromCallingExecutables();
  }

  static void preventJGitFromCallingExecutables() {
    SystemReader reader = SystemReader.getInstance();
    SystemReader.setInstance(
      new DelegatingSystemReader(reader) {
        @Override
        public String getenv(String variable) {
          if ("PATH".equals(variable)) {
            return "";
          } else {
            return super.getenv(variable);
          }
        }
      }
    );
  }

  private final Logger log = LoggerFactory.getLogger(SemVerPlugin.class);

  @Override
  public void apply(Project project) {
    var reader = SystemReader.getInstance();
    var builder = new FileRepositoryBuilder()
      .readEnvironment(reader)
      .setMustExist(true)
      .findGitDir(project.getProjectDir());

    try (var repo = builder.build()) {
      var porcelainGit = new PorcelainGit(new Git(repo));

      Supplier<String> gitVersion = () ->
        Optional
          .ofNullable(porcelainGit.describe())
          .map(v -> v.substring(1))
          .map(v -> v.contains("g") ? v + "-SNAPSHOT" : v)
          .orElse(null);

      // project.getExtensions().getExtraProperties().set("gitVersion", gitVersion);

      var version = gitVersion.get();
      if (version != null) {
        project.setVersion(version);
      }
    } catch (Exception e) {
      log.error("", e);
    }
  }
}

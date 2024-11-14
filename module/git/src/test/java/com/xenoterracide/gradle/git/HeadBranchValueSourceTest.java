// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import java.net.URISyntaxException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;

class HeadBranchValueSourceTest {

  Project project;
  Git git;

  @BeforeEach
  void setupProject() throws Exception {
    project = ProjectBuilder.builder().build();
    project.getPlugins().apply(GitPlugin.class);

    var msg1 = "one";

    git = new InitCommand().setDirectory(project.getProjectDir()).call();
    git.commit().setMessage(msg1).setAllowEmpty(true).call();
  }

  void addRemote() throws URISyntaxException, GitAPIException {
    git.remoteAdd().setName("origin").setUri(new URIish("https://github.com/xenoterracide/gradle-semver.git")).call();
  }
}

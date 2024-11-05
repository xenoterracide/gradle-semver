// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URISyntaxException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.gradle.api.Project;
import org.gradle.api.internal.provider.MissingValueException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HeadBranchValueSourceTest {

  Project project;
  Git git;

  @Test
  void obtainWithHeadBranchSetNoRemotes() {
    var hbvsp = project
      .getProviders()
      .of(HeadBranchValueSource.class, c -> {
        c.getParameters().getHeadBranch().set("other");
        c.getParameters().getProjectDir().set(project.getProjectDir());
      });

    assertThatExceptionOfType(MissingValueException.class).isThrownBy(() -> hbvsp.get());
  }

  @Test
  void obtainWithHeadBranch() throws URISyntaxException, GitAPIException {
    this.addRemote();
    var hbvsp = project
      .getProviders()
      .of(HeadBranchValueSource.class, c -> {
        c.getParameters().getHeadBranch().set("other");
        c.getParameters().getProjectDir().set(project.getProjectDir());
        c.getParameters().getRemotes().add(new RemotesValueSource.Remote("origin"));
      });

    assertThat(hbvsp.get()).isEqualTo("other");
  }

  @Test
  void obtainFromRemote() throws URISyntaxException, GitAPIException {
    this.addRemote();
    var hbvsp = project
      .getProviders()
      .of(HeadBranchValueSource.class, c -> {
        c.getParameters().getProjectDir().set(project.getProjectDir());
      });

    assertThat(hbvsp.get()).isEqualTo("main");
  }

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

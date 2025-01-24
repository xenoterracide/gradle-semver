// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.semver.test;

import static com.xenoterracide.gradle.git.fixtures.CommitTools.commit;
import static com.xenoterracide.gradle.git.fixtures.CommitTools.supplies;
import static org.assertj.core.api.Assertions.assertThat;

import com.xenoterracide.gradle.semver.SemverExtension;
import com.xenoterracide.gradle.semver.SemverPlugin;
import java.io.File;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.URIish;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.semver4j.Semver;

class SemverBuilderIntegrationTest {

  static final Pattern VERSION_PATTERN = Pattern.compile(
    "^\\d+\\.\\d+\\.\\d+-\\p{Alpha}+\\.\\d+\\.\\d+\\+git\\.\\d+\\.\\p{XDigit}{7}$"
  );

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  File projectDir;

  @Test
  void semver() throws Exception {
    var pb = ProjectBuilder.builder().withProjectDir(projectDir);
    var main = "main";
    try (var git = Git.init().setDirectory(projectDir).setInitialBranch(main).call()) {
      Supplier<Semver> vs = () -> {
        var project = pb.build();
        project.getPluginManager().apply(SemverPlugin.class);
        return project.getExtensions().getByType(SemverExtension.class).getProvider().get();
      };
      var size = 29;
      var v001Alpha01 = supplies(commit(git), vs);

      assertThat(v001Alpha01).asString().startsWith("0.0.1-alpha.0.1+").hasSize(size).matches(VERSION_PATTERN);

      var v001Alpha02 = supplies(commit(git), vs);

      assertThat(v001Alpha02).asString().startsWith("0.0.1-alpha.0.2+").hasSize(size).matches(VERSION_PATTERN);

      git.tag().setName("v0.1.0").call();

      var v010 = vs.get();

      assertThat(v010).isGreaterThan(v001Alpha01);

      var v010BldV2 = supplies(commit(git), vs);

      assertThat(v010BldV2)
        .isGreaterThan(v001Alpha01)
        .isGreaterThan(v010)
        .asString()
        .startsWith("0.1.1-alpha.0.1+")
        .hasSize(size)
        .matches(VERSION_PATTERN);

      assertThat(v010BldV2).isEqualByComparingTo(new Semver("0.1.1-alpha.0.1+2.git.3aae11e"));

      var v010BldV3 = supplies(commit(git), vs);

      assertThat(v010BldV3)
        .isGreaterThan(v001Alpha01)
        .isGreaterThan(v010)
        .isGreaterThan(v010BldV2)
        .asString()
        .startsWith("0.1.1-alpha.0.2+")
        .matches(VERSION_PATTERN);

      git.tag().setName("v0.1.1-rc.1").call();

      var v011Rc1 = vs.get();

      assertThat(v011Rc1)
        .isGreaterThan(v001Alpha01)
        .isGreaterThan(v010)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010BldV3)
        .asString()
        .isEqualTo("0.1.1-rc.1");

      git.tag().setName("v0.1.1").call();

      var v011 = vs.get();

      assertThat(v011)
        .isGreaterThan(v010BldV2)
        .isGreaterThan(v010)
        .isGreaterThan(v001Alpha01)
        .hasToString("0.1.1")
        .extracting(Semver::getMajor, Semver::getMinor, Semver::getPatch, Semver::getPreRelease, Semver::getBuild)
        .containsExactly(0, 1, 1, Collections.emptyList(), Collections.emptyList());

      git.remoteAdd().setUri(new URIish("https://example.com/repo.git")).setName("origin").call();
      new ProcessBuilder("git", "remote", "set-head", "origin", "main")
        .directory(projectDir)
        .start()
        .waitFor(1, TimeUnit.SECONDS);

      commit(git);
      var branch = "topic/foo";
      git.checkout().setCreateBranch(true).setName(branch).call();
      assertThat(vs.get())
        .isGreaterThan(v011)
        .asString()
        .startsWith("0.1.2-alpha.0.1+branch.topic-foo.git.1.")
        .hasSize(size)
        .matches(VERSION_PATTERN);
      commit(git);
      commit(git);

      git.checkout().setName(main).call();

      commit(git);
      assertThat(vs.get())
        .isGreaterThan(v011)
        .asString()
        .startsWith("0.1.2-alpha.0.2+git.2.")
        .hasSize(size)
        .matches(VERSION_PATTERN);

      git.checkout().setName(branch).call().getObjectId();

      assertThat(vs.get())
        .isGreaterThan(v011)
        .asString()
        .startsWith("0.1.2-alpha.0.1+branch.topic-foo.git.3.")
        .hasSize(size)
        .matches(VERSION_PATTERN);
    }
  }
}

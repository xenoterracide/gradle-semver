// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import static com.xenoterracide.gradle.git.internal.GitTools.getHeadBranch;
import static com.xenoterracide.gradle.git.internal.GitTools.openGit;
import static com.xenoterracide.gradle.git.internal.GradleTools.finalizeOnRead;

import com.xenoterracide.gradle.git.internal.GitTools;
import io.vavr.control.Try;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ValueSource;
import org.gradle.process.ExecOperations;
import org.gradle.process.internal.ExecException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value source for the head branch.
 * <ol>
 *   <li>check for remotes</li>
 *   <li>check if set explicitly</li>
 *   <li>check git netork</li>
 * </ol>
 */
public abstract class HeadBranchValueSource
  implements ValueSource<String, HeadBranchValueSource.HeadBranchValueSourceParameters> {

  private final ExecOperations execOperations;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject
  public HeadBranchValueSource(ExecOperations execOperations) {
    this.execOperations = execOperations;
  }

  @Override
  public @Nullable String obtain() {
    var remote = finalizeOnRead(this.getParameters().getRemote());
    var projectDir = finalizeOnRead(this.getParameters().getProjectDir());
    var git = openGit(projectDir::get);

    try (var baos = new ByteArrayOutputStream()) {
      this.execOperations.exec(execSpec -> {
          execSpec.setExecutable("git");
          execSpec.args("remote", "show", "origin");
          execSpec.setStandardOutput(baos);
        }).getExitValue();

      // string conversion warning is only valid 17+ not for our 11
      return GitTools.parseHeadBranch(new String(baos.toByteArray(), StandardCharsets.UTF_8));
    } catch (IOException e) {
      this.log.warn("Git had an exception", e);
    }
    return null;
  }

  private void setHeadBranchFor(Provider<String> remote) throws ExecException {
    try {
      this.execOperations.exec(execSpec -> {
          execSpec.setExecutable("git");
          execSpec.args("remote", "set-head", remote.get(), "--auto");
        }).rethrowFailure();
    } catch (ExecException e) {
      this.log.warn("Refs not found, fetching refs");

      this.execOperations.exec(execSpec -> {
          execSpec.setExecutable("git");
          execSpec.args("fetch", "--filter=blob:none", remote.get());
        }).rethrowFailure();

      this.execOperations.exec(execSpec -> {
          execSpec.setExecutable("git");
          execSpec.args("remote", "set-head", remote.get(), "--auto");
        }).rethrowFailure();
    }
  }

  private String getAndMaybeSetHeadBranchFor(Try.WithResources1<Git> git, Provider<String> remote) {
    Try.run(() -> setHeadBranchFor(remote));
    git.of(getHeadBranch(remote::get));
    return "";
  }

  public interface HeadBranchValueSourceParameters extends GitValueSourceParameters {
    Property<String> getRemote();
  }
}

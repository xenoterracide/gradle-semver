// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import com.xenoterracide.gradle.git.internal.GitUtils;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.jgit.api.Git;
import org.gradle.api.provider.ValueSource;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.Nullable;
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
public abstract class HeadBranchValueSource implements ValueSource<String, GitConfigurationExtension> {

  private final ExecOperations execOperations;
  private final GitSupplierService gitS;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject
  public HeadBranchValueSource(ExecOperations execOperations, GitSupplierService gitS) {
    this.execOperations = execOperations;
    this.gitS = gitS;
  }

  @Override
  public @Nullable String obtain() {
    var hasRemotes =
      this.gitS.get()
        .map(Git::remoteList)
        .map(cmd -> Try.of(cmd::call))
        .map(t -> t.onFailure(e -> this.log.warn("Failed to get remotes", e)))
        .map(t -> t.get())
        .map(remotes -> !remotes.isEmpty())
        .orElse(false);

    if (hasRemotes) {
      return Optional.ofNullable(this.getParameters().getHeadBranch().getOrNull()).orElseGet(() -> {
        return Try.withResources(ByteArrayOutputStream::new)
          .of(this::gitRemoteShow)
          .onFailure(e -> this.log.warn("Failed to get head branch", e))
          .getOrNull();
      });
    }
    return null;
  }

  String gitRemoteShow(ByteArrayOutputStream baos) {
    this.execOperations.exec(execSpec -> {
        execSpec.setExecutable("git");
        execSpec.args("remote", "show", this.getParameters().getSourceRemote());
        execSpec.setStandardOutput(baos);
      }).getExitValue();

    return Objects.requireNonNull(GitUtils.getHeadBranch(baos), "unable to locate HEAD branch in output");
  }
}

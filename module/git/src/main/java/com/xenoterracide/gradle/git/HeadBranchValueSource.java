// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import com.xenoterracide.gradle.git.internal.GitUtils;
import io.vavr.control.Try;
import java.io.IOException;
import java.util.Optional;
import javax.inject.Inject;
import kotlin.text.Charsets;
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
public abstract class HeadBranchValueSource implements ValueSource<String, HeadBranchValueSourceParameters> {

  private final ExecOperations execOperations;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject
  public HeadBranchValueSource(ExecOperations execOperations) {
    this.execOperations = execOperations;
  }

  @Override
  public @Nullable String obtain() {
    try (var git = Git.open(this.getParameters().getProjectDir().get())) {
      var hasRemotes = Optional.of(git)
        .map(Git::remoteList)
        .map(cmd -> Try.of(cmd::call))
        .map(t -> t.onFailure(e -> this.log.warn("Failed to get remotes", e)))
        .map(t -> t.get())
        .map(remotes -> !remotes.isEmpty())
        .orElse(false);

      if (hasRemotes) {
        var hb = this.getParameters().getHeadBranch().getOrNull();
        return Optional.ofNullable(hb).orElseGet(() -> {
          return Try.withResources(ByteArrayOutputStream::new)
            .of(this::gitRemoteShow)
            .onFailure(e -> this.log.warn("problem executing git remote show", e))
            .map(GitUtils::parseHeadBranch)
            .getOrNull();
        });
      } else {
        this.log.warn("No remotes found, unable to determine head branch");
      }
    } catch (IOException e) {
      this.log.warn("Git had an exception", e);
    }
    return null;
  }

  String gitRemoteShow(ByteArrayOutputStream baos) {
    this.execOperations.exec(execSpec -> {
        execSpec.setExecutable("git");
        execSpec.args("remote", "show", this.getParameters().getSourceRemote().get());
        execSpec.setStandardOutput(baos);
      }).getExitValue();

    return baos.toString(Charsets.UTF_8);
  }
}

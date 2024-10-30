// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import com.xenoterracide.gradle.git.internal.GitUtils;
import java.io.IOException;
import javax.inject.Inject;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.ValueSource;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.Nullable;

abstract class HeadBranchValueSource implements ValueSource<String, GitConfigurationExtension> {

  private final ExecOperations execOperations;

  @Inject
  protected HeadBranchValueSource(Logger log, ExecOperations execOperations) {
    this.execOperations = execOperations;
  }

  @Override
  public @Nullable String obtain() {
    try (var baos = new ByteArrayOutputStream()) {
      this.execOperations.exec(execSpec -> {
          execSpec.setExecutable("git");
          execSpec.args("remote", "show", this.getParameters().getSourceRemote());
          execSpec.setStandardOutput(baos);
        }).getExitValue();

      return GitUtils.getHeadBranch(baos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}

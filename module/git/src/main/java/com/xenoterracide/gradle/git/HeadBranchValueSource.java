// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.ValueSource;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.Nullable;

abstract class HeadBranchValueSource implements ValueSource<String, GitConfigurationExtension> {

  private final Logger log;
  private final ExecOperations execOperations;

  @Inject
  protected HeadBranchValueSource(Logger log, ExecOperations execOperations) {
    this.log = log;
    this.execOperations = execOperations;
  }

  @Override
  public @Nullable String obtain() {
    try (var baos = new ByteArrayOutputStream()) {
      var exec = execOperations
        .exec(execSpec -> {
          execSpec.setExecutable("git");
          execSpec.args("remote", "show", this.getParameters().getSourceRemote());
          execSpec.setStandardOutput(baos);
        })
        .getExitValue();
      return baos.toString(StandardCharsets.UTF_8).trim();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}

// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: Apache-2.0

package com.xenoterracide.gradle.git;

import static com.xenoterracide.gradle.git.internal.GradleTools.finalizeOnRead;

import com.xenoterracide.gradle.git.internal.GitTools;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.process.ExecOperations;
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
    try (var baos = new ByteArrayOutputStream()) {
      this.execOperations.exec(execSpec -> {
          execSpec.setExecutable("git");
          execSpec.args("rev-parse", "--abbrev-ref", "HEAD");
          execSpec.setStandardOutput(baos);
        }).getExitValue();

      // string conversion warning is only valid 17+ not for our 11
      return new String(baos.toByteArray(), StandardCharsets.UTF_8).trim();
    } catch (IOException e) {
      this.log.warn("Git had an exception", e);
    }

    try (var baos = new ByteArrayOutputStream()) {
      this.execOperations.exec(execSpec -> {
          execSpec.setExecutable("git");
          execSpec.args("remote", "show", finalizeOnRead(this.getParameters().getRemote()));
          execSpec.setStandardOutput(baos);
        }).getExitValue();

      // string conversion warning is only valid 17+ not for our 11
      return GitTools.parseHeadBranch(new String(baos.toByteArray(), StandardCharsets.UTF_8));
    } catch (IOException e) {
      this.log.warn("Git had an exception", e);
    }
    return null;
  }

  public interface HeadBranchValueSourceParameters extends ValueSourceParameters {
    Property<String> getRemote();
  }
}

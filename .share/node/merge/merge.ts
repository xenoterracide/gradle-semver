#!/usr/bin/env tsx

// SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
//
// SPDX-License-Identifier: GPL-3.0-or-later

import { execFileSync, execSync } from "child_process";
import { existsSync, mkdtempSync, readFileSync, rmSync, unlinkSync, writeFileSync } from "fs";
import { tmpdir } from "os";
import { join } from "path";

const ENGINE = process.env.ENGINE || "kimi";

function run(cmd: string, opts?: { cwd?: string; env?: Record<string, string> }): string {
  return execSync(cmd, { encoding: "utf8", cwd: opts?.cwd, env: { ...process.env, ...opts?.env } }).trim();
}

function runSilent(cmd: string, args: string[], opts?: { cwd?: string }): string {
  try {
    return execFileSync(cmd, args, { encoding: "utf8", cwd: opts?.cwd }).trim();
  } catch {
    return "";
  }
}

function hasPR(): boolean {
  try {
    run("gh pr view --json number");
    return true;
  } catch {
    return false;
  }
}

function getHead(): string {
  return run("git rev-parse --verify HEAD");
}

async function generateMessage(titleFile: string, bodyFile: string, tmpDir: string): Promise<void> {
  const diffRange = "origin/HEAD...HEAD";

  // Check for changes
  try {
    run(`git diff --quiet --exit-code ${diffRange}`);
    console.log("No changes to generate message for");
    process.exit(2);
  } catch {
    // Has changes, continue
  }

  const changedFiles = run(`git diff --name-only ${diffRange}`).split("\n").slice(0, 400).join("\n");
  const changedDiff = run(`git diff ${diffRange}`).split("\n").slice(0, 2000).join("\n");

  if (ENGINE === "kimi") {
    await generateWithKimi(titleFile, bodyFile, changedDiff, tmpDir);
  } else if (ENGINE === "junie") {
    await generateWithJunie(titleFile, bodyFile, changedDiff, tmpDir);
  } else {
    await generateWithCopilot(titleFile, bodyFile, changedDiff, changedFiles, tmpDir);
  }
}

async function generateWithKimi(titleFile: string, bodyFile: string, diff: string, tmpDir: string): Promise<void> {
  const skillsDir = ".agents/skills";
  const hasSkillsDir = existsSync(skillsDir);

  const prompt = `Generate a conventional commit message for the following diff and write the subject line to '${titleFile}' and the body to '${bodyFile}'. Do not run any tests or gradle commands.

Diff:
${diff}`;

  // Write prompt to temp file and use shell redirection to avoid command line length limits
  const promptFile = join(tmpDir, "kimi-prompt.txt");
  writeFileSync(promptFile, prompt, "utf8");

  const kimiArgs = ["--no-thinking", "--quiet"];
  if (hasSkillsDir) {
    kimiArgs.unshift("--skills-dir", skillsDir);
  }

  const kimiOut = join(tmpDir, "kimi-out.txt");

  try {
    try {
      // Use shell to redirect file content to kimi via stdin
      // This avoids command injection while handling large prompts
      execSync(
        `kimi ${kimiArgs.join(" ")} --prompt "$(cat '${promptFile.replace(/'/g, "'\"'\"'")}')" > "${kimiOut}" 2>&1 || true`,
        {
          encoding: "utf8",
          shell: "/bin/bash",
        },
      );
    } catch {
      // Ignore errors, check output below
    }

    // Check if kimi wrote directly to files
    try {
      readFileSync(titleFile, "utf8");
      console.log("kimi wrote title/body directly");
      return;
    } catch {
      // Didn't write directly, use captured output if available
      if (!existsSync(kimiOut)) {
        throw new Error("kimi failed to generate message");
      }
      const output = readFileSync(kimiOut, "utf8");
      await parseAndWriteMessage(output, titleFile, bodyFile);
    }
  } finally {
    try {
      unlinkSync(promptFile);
      unlinkSync(kimiOut);
    } catch {}
  }
}

async function generateWithJunie(titleFile: string, bodyFile: string, diff: string, tmpDir: string): Promise<void> {
  const promptFile = join(tmpDir, "junie-prompt.txt");
  const prompt = `Generate a conventional commit message for the following diff and write the subject line to '${titleFile}' and the body to '${bodyFile}'. Do not run any tests or gradle commands.

Diff:
${diff}`;
  writeFileSync(promptFile, prompt, "utf8");

  try {
    execFileSync("junie", ["--skip-update-check", "--cache-dir=.junie/cache", "--prompt-file", promptFile], {
      encoding: "utf8",
    });
    // Check if junie wrote directly
    readFileSync(titleFile, "utf8");
    return;
  } catch {
    // Try to capture output
    try {
      const output = execFileSync(
        "junie",
        ["--skip-update-check", "--cache-dir=.junie/cache", "--output-format=json", "--prompt-file", promptFile],
        { encoding: "utf8" },
      );
      const result = execFileSync("jq", ["-r", ".result"], { input: output, encoding: "utf8" });
      await parseAndWriteMessage(result.trim(), titleFile, bodyFile);
    } catch {
      throw new Error("junie failed to generate message");
    }
  } finally {
    try {
      unlinkSync(promptFile);
    } catch {}
  }
}

async function generateWithCopilot(
  titleFile: string,
  bodyFile: string,
  diff: string,
  files: string,
  tmpDir: string,
): Promise<void> {
  const allowedTypes = "ci feat fix perf refactor style test build ops docs chore merge revert";

  const promptFile = join(tmpDir, "copilot-prompt.txt");
  const prompt = `You are writing a git commit message for a human developer.

You MUST follow this exact template:

<type>(<scope>): <summary>

<body>

Rules:
- Output plain text only. No markdown fences.
- First line MUST be a valid Conventional Commit subject.
- Keep the FIRST line <= 72 characters.
- Use a specific scope when possible.
- Body:
  - Provide 0-6 bullet points.
  - Explain WHAT changed and WHY.
  - Wrap body lines to <= 72 characters.
  - Do not repeat the subject.

IMPORTANT: Use ONLY these commit types: ${allowedTypes}
If unsure, choose 'chore'.

Changed files:
${files}

Diff:
${diff}`;
  writeFileSync(promptFile, prompt, "utf8");

  const copilotOut = join(tmpDir, "copilot-out.txt");
  const copilotErr = join(tmpDir, "copilot-err.txt");

  try {
    const model = process.env.COPILOT_PRMSG_MODEL || "gpt-5.1-codex-mini";
    try {
      const result = execFileSync("copilot", ["--model", model, "-s", "-p", promptFile], {
        encoding: "utf8",
      });
      writeFileSync(copilotOut, result, "utf8");
    } catch (e: unknown) {
      // Capture stderr on error
      const stderr = e && typeof e === "object" && "stderr" in e ? String((e as { stderr: unknown }).stderr) : "";
      writeFileSync(copilotErr, stderr, "utf8");
    }

    let output = readFileSync(copilotOut, "utf8");
    const err = readFileSync(copilotErr, "utf8");

    if (!output && err.includes("enable this model")) {
      const fallback = process.env.COPILOT_PRMSG_FALLBACK_MODEL || "gpt-5.1-codex";
      try {
        const result = execFileSync("copilot", ["--model", fallback, "-s", "-p", promptFile], {
          encoding: "utf8",
        });
        writeFileSync(copilotOut, result, "utf8");
      } catch (e: unknown) {
        // Ignore errors
      }
      output = readFileSync(copilotOut, "utf8");
    }

    await parseAndWriteMessage(output, titleFile, bodyFile);
  } finally {
    try {
      unlinkSync(promptFile);
      unlinkSync(copilotOut);
      unlinkSync(copilotErr);
    } catch {}
  }
}

async function parseAndWriteMessage(aiOutput: string, titleFile: string, bodyFile: string): Promise<void> {
  const allowedTypes = "ci feat fix perf refactor style test build ops docs chore merge revert";
  const lines = aiOutput.replace(/\r/g, "").split("\n");

  // Find subject line matching conventional commit pattern
  let subject = "";
  const allowedTypesAlt = allowedTypes.replace(/ /g, "|");
  // Match: type(scope): ..., type!: ..., or type(scope)!: ...
  const pattern = new RegExp(`^(${allowedTypesAlt})(\\(([^)]+)\\))?(!)?: .+`);

  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    const match = trimmed.match(pattern);
    if (match) {
      subject = trimmed;
      break;
    }
  }

  // Clean up common prefixes
  subject = subject.replace(/^(I'll|I will|Sure,?|Here's|Proposed|Suggested)[: -]+/i, "");

  if (!subject) {
    // Check if there's an existing valid PR message to preserve
    try {
      const existing = readFileSync(titleFile, "utf8").trim();
      if (existing && !existing.match(/^error\([^)]+\):/)) {
        console.log("Preserving existing PR message");
        process.exit(0);
      }
    } catch {}

    console.error("ERROR: Failed to generate valid conventional commit subject");
    if (aiOutput) {
      console.error("--- AI Output ---");
      console.error(aiOutput);
    }
    process.exit(1);
  }

  // Extract body (lines after first blank line after subject)
  let subjectFound = false;
  let blankFound = false;
  const bodyLines: string[] = [];

  for (const line of lines) {
    if (!subjectFound) {
      if (line.trim() === subject) subjectFound = true;
      continue;
    }
    if (!blankFound) {
      if (line.trim() === "") blankFound = true;
      continue;
    }
    bodyLines.push(line);
  }

  // Clean up body
  const body = bodyLines
    .filter((l) => !l.match(/^(I'll|I will|Sure|Here's|Proposed|Suggested|I inspected|Let's)\b/i))
    .slice(0, 12)
    .join("\n")
    .trim();

  writeFileSync(titleFile, subject.substring(0, 72) + "\n", "utf8");
  writeFileSync(bodyFile, body, "utf8");
}

async function waitForChecks(): Promise<void> {
  console.log("Waiting for PR checks to complete...");

  try {
    // gh pr checks --fail-fast --watch will:
    // - Wait for checks to appear if none exist yet
    // - Watch all checks and exit with error if any fail
    // - Exit successfully when all checks pass
    execFileSync("gh", ["pr", "checks", "--fail-fast", "--watch"], { stdio: "inherit" });
  } catch {
    console.error("Error: PR checks failed or timed out.");
    process.exit(1);
  }
}

async function main(): Promise<void> {
  const args = process.argv.slice(2);
  const command = args[0];
  const dryRun = args.includes("--dry-run");

  if (command === "pr-message") {
    // CLI mode for pr-message
    let titleFile = "";
    let bodyFile = "";

    for (let i = 1; i < args.length; i += 2) {
      if (args[i] === "--title-file") titleFile = args[i + 1];
      if (args[i] === "--body-file") bodyFile = args[i + 1];
    }

    if (!titleFile || !bodyFile) {
      console.error("Usage: merge.ts pr-message --title-file PATH --body-file PATH");
      process.exit(2);
    }

    const tmpDir = mkdtempSync(join(tmpdir(), "prmsg-"));
    try {
      await generateMessage(titleFile, bodyFile, tmpDir);
    } finally {
      try {
        rmSync(tmpDir, { recursive: true, force: true });
      } catch {}
    }
    return;
  }

  // Full merge workflow
  console.log("Fetching and merging origin/HEAD...");
  run("git fetch --all --prune --prune-tags --tags --force");
  run("git merge origin/HEAD");

  console.log("Pushing...");
  run("git push");

  const hasExistingPR = hasPR();

  if (hasExistingPR) {
    await waitForChecks();
    await createOrUpdatePR();
  } else {
    await createOrUpdatePR();
    await waitForChecks();
  }

  if (dryRun) {
    console.log("\n[Dry Run] Would proceed with squash merge. Exiting without merging.");
    process.exit(0);
  }

  // Merge squash
  const hasUncommitted = runSilent("git", ["status", "--porcelain=1"]) !== "";
  if (hasUncommitted) {
    console.warn("WARNING: Uncommitted changes detected. Review before merge.");
  }

  process.stdout.write("Proceed with squash merge? [Y/n] ");

  if (!process.stdin.isTTY) {
    console.error("Interactive confirmation required, but no TTY is available. Aborting squash merge.");
    process.exit(1);
  }

  const reply = await new Promise<string>((resolve) => {
    process.stdin.once("data", (data) => resolve(data.toString().trim().toLowerCase()));
  });

  if (reply === "n" || reply === "no") {
    console.log("Merge cancelled.");
    process.exit(1);
  }

  run("gh pr merge --squash --delete-branch");
  process.exit(0);
}

async function createOrUpdatePR(): Promise<void> {
  const tmpDir = mkdtempSync(join(tmpdir(), "pr-"));
  const titleFile = join(tmpDir, "title.txt");
  const bodyFile = join(tmpDir, "body.txt");

  const headBefore = getHead();

  try {
    if (hasPR()) {
      console.log("Updating PR message...");
    }

    await generateMessage(titleFile, bodyFile, tmpDir);
    const headAfter = getHead();

    // Regenerate if HEAD changed during generation
    if (headBefore !== headAfter) {
      await generateMessage(titleFile, bodyFile, tmpDir);
    }

    const title = readFileSync(titleFile, "utf8").trim();

    if (hasPR()) {
      run(`gh pr edit --title "${title.replace(/"/g, '\\"')}" --body-file "${bodyFile}"`);
      run("GH_PAGER=cat gh pr view");
    } else {
      run(`gh pr create --title "${title.replace(/"/g, '\\"')}" --body-file "${bodyFile}"`);
      console.log("PR created with generated message.");
      run("GH_PAGER=cat gh pr view");
    }
  } finally {
    try {
      rmSync(tmpDir, { recursive: true, force: true });
    } catch {}
  }
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});

// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: MIT

buildscript { dependencyLocking { lockAllConfigurations() } }

fun gitOutput(vararg args: String): String? =
  try {
    providers
      .exec { commandLine("git", *args) }
      .standardOutput.asText
      .get()
      .trim()
      .takeIf { it.isNotEmpty() }
  } catch (_: Exception) {
    null
  }

fun sanitizeBranchName(branch: String): String = branch.replace(Regex("[^a-zA-Z0-9]"), "-")

fun incrementPatch(version: String): String {
  val core = version.substringBefore('-').split('.')
  return "${core[0]}.${core[1]}.${core[2].toInt() + 1}"
}

fun calculateProjectVersion(): String {
  val currentBranch = gitOutput("branch", "--show-current")
  val headBranch = gitOutput("symbolic-ref", "refs/remotes/origin/HEAD")?.removePrefix("refs/remotes/origin/")
  val nearestTag = gitOutput("describe", "--tags", "--match", "v[0-9]*.[0-9]*.[0-9]*", "--abbrev=0")
  val distanceFromTag =
    nearestTag?.let { gitOutput("rev-list", "--count", "$it..HEAD") } ?: gitOutput(
      "rev-list",
      "--count",
      "HEAD",
    ) ?: "0"
  val shortSha = gitOutput("rev-parse", "--short", "HEAD") ?: "unknown"
  val isHeadBranch = headBranch == null || currentBranch == headBranch
  val distanceFromMergeBase =
    if (isHeadBranch || headBranch == null) {
      distanceFromTag
    } else {
      gitOutput("merge-base", "HEAD", "origin/$headBranch")
        ?.let { gitOutput("rev-list", "--count", "$it..HEAD") }
        ?: distanceFromTag
    }

  if (nearestTag == null) {
    val metadata =
      if (isHeadBranch) {
        "git.$distanceFromTag.$shortSha"
      } else {
        "branch.${sanitizeBranchName(currentBranch ?: "unknown")}.git.$distanceFromMergeBase.$shortSha"
      }
    return "0.0.1-alpha.0.$distanceFromTag+$metadata"
  }

  val baseVersion = nearestTag.removePrefix("v")
  if (distanceFromTag == "0") {
    return if (currentBranch.isNullOrBlank() || isHeadBranch) {
      baseVersion
    } else {
      "$baseVersion+branch.${sanitizeBranchName(currentBranch)}.git.0.$shortSha"
    }
  }

  val coreVersion = if ('-' in baseVersion) baseVersion.substringBefore('-') else incrementPatch(baseVersion)
  val prerelease =
    if ('-' in baseVersion) {
      "${baseVersion.substringAfter('-')}.$distanceFromTag"
    } else {
      "alpha.0.$distanceFromTag"
    }
  val metadata =
    if (isHeadBranch) {
      "git.$distanceFromTag.$shortSha"
    } else {
      "branch.${sanitizeBranchName(currentBranch ?: "unknown")}.git.$distanceFromMergeBase.$shortSha"
    }
  return "$coreVersion-$prerelease+$metadata"
}

plugins {
  `java-base`
  alias(libs.plugins.dependency.analysis)
}

group = "com.xenoterracide.gradle"

dependencyLocking {
  lockAllConfigurations()
}

version = if (providers.environmentVariable("IS_PUBLISHING").isPresent) calculateProjectVersion() else "0.0.0"

val versionText = version.toString()

tasks.register<Exec>("semverVersion") {
  group = "Help"
  description = "Prints the semantic version computed for this build"
  commandLine("printf", "%s\\n", versionText)
}

tasks.register<Exec>("version") {
  group = "Help"
  description = "Prints project.version"
  commandLine("printf", "%s\\n", versionText)
}

tasks.dependencies {
  dependsOn(subprojects.map { it.tasks.dependencies })
}

tasks.check {
  dependsOn(tasks.buildHealth)
}

dependencyAnalysis {
  issues {
    project(":semver") {
      onAny {
        exclude(libs.semver)
      }
    }
    all {
      onAny {
        severity("fail")
        exclude("org.slf4j:slf4j-api")
      }
      onUnusedDependencies {
        exclude(libs.junit.parameters)
        exclude("org.junit.jupiter:junit-jupiter")
      }
      ignoreSourceSet("testIntegration")
    }
  }
}

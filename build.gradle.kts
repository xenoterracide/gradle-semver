import org.semver4j.Semver

// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: MIT

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  `java-base`
  alias(libs.plugins.dependency.analysis)
  alias(libs.plugins.semver)
}

group = "com.xenoterracide.gradle"

dependencyLocking {
  lockAllConfigurations()
}

version =
  providers
    .environmentVariable("IS_PUBLISHING")
    .flatMap { semver.provider }
    .getOrElse(Semver.ZERO)

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
      }
      ignoreSourceSet("testIntegration")
    }
  }
}

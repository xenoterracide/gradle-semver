// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: MIT

import org.semver4j.Semver

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

version = providers.environmentVariable("IS_PUBLISHING")
  .map { semver.gitDescribed }
  .orElse(Semver("0.0.0")).get().toString()

tasks.check {
  dependsOn(tasks.buildHealth)
}

dependencyAnalysis {
  issues {
    all {
      onAny {
        severity("fail")
      }
      onUnusedDependencies {
        exclude(libs.junit.parameters)
      }
    }
  }
}

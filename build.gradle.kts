import org.semver4j.Semver

// © Copyright 2024 Caleb Cushing
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
    .map { semver.provider().get() }
    .orElse(Semver("0.0.0"))
    .get()

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
      }
      onUnusedDependencies {
        exclude(libs.junit.parameters)
      }
    }
  }
}

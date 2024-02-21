// SPDX-License-Identifier: MIT
// Copyright © 2024 Caleb Cushing.

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  `java-gradle-plugin`
  id("our.javalibrary")
  alias(libs.plugins.dependency.analysis)
  alias(libs.plugins.gradle.plugin.publish)
}

version = "0.9.0"
group = "com.xenoterracide"
repositories {
  mavenCentral()
}

dependencyLocking {
  lockAllConfigurations()
}

dependencies {
  compileOnlyApi(libs.jspecify)
  implementation(libs.jgit)
  implementation(libs.vavr)
  api(libs.semver)
  testImplementation(libs.junit.api)
  testImplementation(gradleTestKit())
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

gradlePlugin {
  plugins {
    create("plugin") {
      id = "com.xenoterracide.gradle.semver"
      displayName = "Semver with Git"
      description = "A semantic versioning plugin that derives the version from git tags and commits and is configuration cache safe."
      tags = setOf("semver", "versioning", "git")
      implementationClass = "com.xenoterracide.gradle.semver.SemverPlugin"
    }
  }
}

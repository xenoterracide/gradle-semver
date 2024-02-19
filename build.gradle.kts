// SPDX-License-Identifier: MIT
// Copyright © 2024 Caleb Cushing.

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  `java-gradle-plugin`
  id("our.javalibrary")
  alias(libs.plugins.dependency.analysis)
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
  implementation(libs.jgit)
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

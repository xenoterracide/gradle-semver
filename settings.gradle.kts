// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: MIT

rootProject.name = "gradle-semver"

pluginManagement {
  repositories {
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.enterprise") version ("3.16.2")
}

gradleEnterprise {
  buildScan {
    publishOnFailureIf(providers.environmentVariable("CI").isPresent)
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

  repositories {
    maven("https://maven.pkg.github.com/xenoterracide/java-commons") {
      name = "gh"
      mavenContent {
        includeModule("com.xenoterracide", "tools")
        snapshotsOnly()
      }
      credentials {
        // use properties because gradles credentials errors if missing
        providers.gradleProperty("ghUsername").let { username = it.orNull }
        providers.gradleProperty("ghPassword").let { password = it.orNull }
        // avoid congiguration cache missing on credentials
        if (username == null || password == null) {
          username = System.getenv("GITHUB_ACTOR")
          password = System.getenv("GITHUB_TOKEN")
        }
      }
    }
    mavenCentral()
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

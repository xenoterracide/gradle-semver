// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: MIT

rootProject.name = "gradle-semver"

pluginManagement {
  repositories {
    maven("https://maven.pkg.github.com/xenoterracide/gradle-semver") {
      name = "gh"
      mavenContent {
        includeGroup("com.xenoterracide.gradle.semver")
        includeModule("com.xenoterracide.gradle", "semver")
      }
      credentials(PasswordCredentials::class)
    }
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.develocity") version ("3.18.2")
}

develocity {
  val ci = providers.environmentVariable("CI")
  buildScan {
    publishing.onlyIf { ci.isPresent }
    termsOfUseUrl.set("https://gradle.com/terms-of-service")
    termsOfUseAgree.set("yes")
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
  rulesMode = RulesMode.FAIL_ON_PROJECT_RULES

  repositories {
    maven("https://maven.pkg.github.com/xenoterracide/gradle-semver") {
      name = "gh"
      mavenContent {
        includeModule("com.xenoterracide", "tools")
      }
      credentials(PasswordCredentials::class)
    }
    mavenCentral()
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootDir.resolve("module").listFiles()?.forEach { file ->
  if (file.isDirectory &&
    file
      ?.list { _, name -> name.startsWith("build.gradle") }
      ?.isNotEmpty() == true
  ) {
    val name = file.name
    include(":$name")
    project(":$name").projectDir = file("module/$name")
  }
}

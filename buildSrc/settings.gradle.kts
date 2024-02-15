// SPDX-License-Identifier: MIT
// Copyright © 2023-2024 Caleb Cushing.
rootProject.name = "buildSrc"

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

  repositories {
    mavenCentral()
    gradlePluginPortal() // this should only be necessary in buildSrc/settings.gradle.kts
  }
}

// SPDX-FileCopyrightText: Copyright © 2023-2026 Caleb Cushing
//
// SPDX-License-Identifier: MIT

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
    gradlePluginPortal()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

  repositories {
    gradlePluginPortal() // this should only be necessary in buildSrc/settings.gradle.kts
  }
}

// © Copyright 2023-2024 Caleb Cushing
// SPDX-License-Identifier: MIT

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  `java-library`
}

dependencyLocking {
  lockAllConfigurations()
}

val libs = the<LibrariesForLibs>()

configurations.configureEach {
  exclude(group = "junit", module = "junit")
  exclude(group = "commons-codec", module = "commons-codec")
  exclude(group = "com.googlecode.javaewah", module = "JavaEWAH")

  resolutionStrategy {
    componentSelection {
      all {
        val spotbugs = Regex("^spotbugs.*")
        if (!name.matches(spotbugs) && !candidate.module.matches(spotbugs)) {
          val nonRelease = Regex("^[\\d.]+-(M|ea|beta|alpha).*$")
          if (candidate.version.matches(nonRelease)) reject("no pre-release")
        }

        if (candidate.module == "nullaway") {
          val reason = "crash https://github.com/uber/NullAway/issues/533"
          if (candidate.version.matches(Regex("^0\\.9\\.[34]$"))) reject(reason)
        }
      }
    }
  }
}

configurations.matching { it.name == "runtimeClasspath" || it.name == "testRuntimeClasspath" }.configureEach {
  exclude(group = "com.google.code.findbugs", module = "jsr305")
  exclude(group = "com.google.errorprone", module = "error_prone_annotations")
  exclude(group = "org.checkerframework", module = "checker-qual")
}

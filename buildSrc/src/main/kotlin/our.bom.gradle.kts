// SPDX-License-Identifier: MIT
// Copyright © 2023-2024 Caleb Cushing.
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  `java-library`
}

dependencyLocking {
  lockAllConfigurations()
}

val libs = the<LibrariesForLibs>()

configurations.configureEach {
  exclude(group = "org.slf4j", module = "slf4j-nop")
  exclude(group = "junit", module = "junit")

  resolutionStrategy {
    componentSelection {
      all {
        val spotbugs = Regex("^spotbugs.*")
        if (!name.matches(spotbugs) && !candidate.module.matches(spotbugs)) {
          val nonRelease = Regex("^[\\d.]+-(M|ea|beta).*$")
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

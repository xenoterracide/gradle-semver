// SPDX-FileCopyrightText: Copyright © 2023-2026 Caleb Cushing
//
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
  exclude(group = "org.slf4j", module = "slf4j-nop")
  exclude(group = "junit", module = "junit")
  exclude(group = "commons-codec", module = "commons-codec")
  exclude(group = "com.googlecode.javaewah", module = "JavaEWAH")

  resolutionStrategy {
    componentSelection {
      all {
        if (!candidate.group.matches(Regex("^com.xenoterracide.*"))) {
          val nonRelease = Regex("^[\\d.]+-(M|RC|ea|beta|alpha).*$")
          if (candidate.version.matches(nonRelease)) reject("no pre-release")
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

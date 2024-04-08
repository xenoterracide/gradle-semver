// © Copyright 2023-2024 Caleb Cushing
// SPDX-License-Identifier: MIT

import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  `java-base`
  id("com.github.spotbugs")
}

tasks.withType<SpotBugsTask>().configureEach {
  if (name != "spotbugsMain") {
    enabled = false
  }
  excludeFilter.set(rootProject.file(".config/spotbugs/exclude.xml"))
  effort.set(Effort.MAX)
  reportLevel.set(Confidence.LOW)
  extraArgs = listOf("-longBugCodes")
}

val libs = the<LibrariesForLibs>()

dependencies {
  spotbugs(libs.spotbugs)
}

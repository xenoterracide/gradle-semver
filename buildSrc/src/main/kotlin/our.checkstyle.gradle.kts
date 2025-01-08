// Copyright 2023 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: MIT

import org.gradle.accessors.dm.LibrariesForLibs
import java.util.Locale

plugins {
  checkstyle
}

val libs = the<LibrariesForLibs>()

tasks.withType<Checkstyle>().configureEach {
  isShowViolations = true
}

fun checkstyleConfig(filename: String): File {
  val path = ".config/checkstyle/$filename"
  val f = file(path)
  return if (f.exists()) f else rootProject.file(path)
}

tasks.withType<Checkstyle>().configureEach {
  configFile =
    checkstyleConfig(
      this.name.removePrefix("checkstyle").replaceFirstChar { it.lowercase(Locale.getDefault()) } + ".xml",
    )
}

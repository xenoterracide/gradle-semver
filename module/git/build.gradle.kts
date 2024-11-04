// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: MIT

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  our.javalibrary
  alias(libs.plugins.shadow)
}

tasks.compileJava {
  options.release = 11
}

dependencies {
  compileOnlyApi(libs.jspecify)
  api(libs.jgit)
  implementation(libs.vavr)
  implementation(libs.guava)
  implementation(libs.commons.io)
  implementation(libs.commons.lang)
  testImplementation(libs.junit.api)
  testImplementation(libs.mockito)
  testImplementation(gradleTestKit())
}

tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set("")
  relocate("org.eclipse.jgit", "com.xenoterracide.gradle.semver.jgit")
  relocate("com.google.common", "com.xenoterracide.gradle.semver.guava")
  dependencies {
    exclude { it.moduleGroup == "io.vavr" }
    exclude { it.moduleGroup == "org.slf4j" }
    exclude { it.moduleName == "semver4j" }
  }
}

gradlePlugin {
  plugins {
    register("com.xenoterracide.gradle.git") {
      displayName = "Git Metadata plugin"
      implementationClass = "com.xenoterracide.gradle.git.GitPlugin"
      description = """
        Provides git metadata for use in gradle builds. Is configuration cache safe.
      """.trimIndent()
      tags = setOf("metadata", "git", "vcs", "scm")
      id = name
    }
  }
}

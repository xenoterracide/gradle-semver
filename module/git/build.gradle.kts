// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: MIT

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  our.javalibrary
  `java-test-fixtures`
  alias(libs.plugins.shadow)
}

tasks.compileJava {
  options.release = 11
}

dependencyLocking {
  lockAllConfigurations()
}

dependencies {
  api(libs.jgit)
  api(libs.vavr)
  compileOnlyApi(libs.jspecify)
  implementation(libs.commons.lang)
  implementation(libs.guava)
  implementation(libs.java.tools)
  shadow(libs.vavr)
  testFixturesApi(libs.jgit)
  testFixturesApi(libs.jspecify)
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      dependencies {
        implementation(testFixtures(project()))
      }
    }
  }
}

tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set("")
  relocate("org.eclipse.jgit", "com.xenoterracide.gradle.semver.jgit")
  relocate("com.google.common", "com.xenoterracide.gradle.semver.guava")
  relocate("com.xenoterracide.tools.java", "com.xenoterracide.tools.java")
  dependencies {
    exclude { it.moduleGroup == "io.vavr" }
    exclude { it.moduleGroup == "org.slf4j" }
    exclude { it.moduleName == "semver4j" }
  }
  minimize()
}

gradlePlugin {
  plugins {
    register("com.xenoterracide.gradle.git") {
      displayName = "Git Metadata"
      implementationClass = "com.xenoterracide.gradle.git.GitPlugin"
      description =
        """
        This plugin provides git metadata
        """.trimIndent()
      tags = setOf("git")
      id = name
    }
  }
}

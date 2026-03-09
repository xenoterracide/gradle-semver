// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: MIT

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  our.convention
  `java-test-fixtures`
  alias(libs.plugins.shadow)
}

dependencyLocking {
  lockAllConfigurations()
}

dependencies {
  compileOnlyApi(libs.jgit)
  api(libs.vavr)
  compileOnlyApi(libs.jspecify)
  implementation(libs.commons.lang)
  compileOnly(libs.guava)
  compileOnly(libs.java.tools)
  shadow(libs.java.tools)
  shadow(libs.jgit)
  shadow(libs.guava)
  testFixturesApi(libs.jgit)
  testFixturesApi(libs.jspecify)
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      dependencies {
        implementation(libs.assertj)
        implementation(libs.junit.api)
        implementation(libs.junit.parameters)
        implementation(testFixtures(project()))
      }
    }
  }
}

tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set("")
  relocate("org.eclipse.jgit", "com.xenoterracide.gradle.git.jgit")
  relocate("com.google.common", "com.xenoterracide.gradle.git.guava")
  relocate("com.xenoterracide.tools.java", "com.xenoterracide.git.tools")
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

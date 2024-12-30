// SPDX-FileCopyrightText: Copyright © 2024 Caleb Cushing
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
  compileOnlyApi(libs.jspecify)
  api(libs.jgit)
  implementation(libs.vavr)
  implementation(libs.guava)
  implementation(libs.commons.lang)
  implementation(libs.java.tools)
  testFixturesImplementation(libs.jgit)
  shadow(libs.vavr)
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      dependencies {
        implementation(testFixtures(project()))
        implementation(libs.junit.api)
      }
    }
    val test by getting(JvmTestSuite::class) {
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
    register("com.xenoterracide.gradle.semver") {
      displayName = "Semver with Git"
      implementationClass = "com.xenoterracide.gradle.semver.SemverPlugin"
      description =
        """
        A semantic versioning plugin that derives the version from git tags and commits and is configuration cache safe.
        """.trimIndent()
      tags = setOf("semver", "versioning", "git", "version")
      id = name
    }
  }
}

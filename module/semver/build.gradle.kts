// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
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

dependencyLocking {
  lockAllConfigurations()
}

dependencies {
  api(libs.semver)
  api(projects.git)
  compileOnlyApi(libs.jspecify)
  implementation(libs.commons.lang)
  implementation(libs.guava)
  implementation(libs.java.tools)
  implementation(libs.vavr)
  shadow(libs.jspecify)
  shadow(libs.semver)
  shadow(libs.vavr)
  shadow(projects.git)
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      dependencies {
        implementation(testFixtures(projects.git))
      }
    }
    val test by getting(JvmTestSuite::class) {
      dependencies {
        implementation(libs.maven.artifact)
      }
    }
  }
}

tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set("")
  relocate("com.google.common", "com.xenoterracide.gradle.semver.guava")
  relocate("com.xenoterracide.tools.java", "com.xenoterracide.tools.java")
  dependencies {
    exclude { it.moduleGroup == "io.vavr" }
    exclude { it.moduleGroup == "org.jspecify" }
    exclude { it.moduleGroup == "org.slf4j" }
    exclude { it.moduleName == "com.xenoterracide.gradle.git" }
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

publishing {
  publications {
    register<MavenPublication>("relocation") {
      pom {
        groupId = "com.xenoterracide"
        artifactId = project.name
        version = rootProject.version.toString()

        distributionManagement {
          relocation {
            groupId = rootProject.group.toString()
            artifactId = project.name
            version = rootProject.version.toString()
            message = "groupId has been changed to follow my conventions"
          }
        }
      }
    }
  }
}

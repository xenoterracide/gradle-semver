// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: MIT

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.spotbugs.snom.SpotBugsTask

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  our.convention
  alias(libs.plugins.shadow)
}

dependencyLocking {
  lockAllConfigurations()
}

dependencies {
  annotationProcessor(libs.immutables.core)
  annotationProcessor(platform(libs.immutables.bom))
  compileOnly(libs.bundles.immutables)
  compileOnly(libs.commons.lang)
  compileOnly(libs.java.tools)
  compileOnly(platform(libs.immutables.bom))
  compileOnlyApi(libs.semver)
  compileOnlyApi(projects.git)
  compileOnlyApi(libs.jspecify)
  shadow(libs.commons.lang)
  shadow(libs.semver)
  shadow(projects.git)
  spotbugs(libs.spotbugs)
  testImplementation(libs.jgit)
}

// Ensure SpotBugs has access to annotation classes for proper null analysis
tasks.withType<SpotBugsTask>().configureEach {
  auxClassPaths.from(configurations.compileClasspath)
  auxClassPaths.from(configurations.runtimeClasspath)
}

tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set("")
  relocate("com.xenoterracide.tools", "com.xenoterracide.gradle.semver.tools")
  dependencies {
    include { it.moduleGroup == "com.xenoterracide" && it.moduleName == "tools" }
  }
  minimize()
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      dependencies {
        implementation(libs.assertj)
        implementation(libs.junit.api)
        implementation(libs.junit.parameters)
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

// SPDX-FileCopyrightText: Copyright © 2024 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: MIT

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  our.javalibrary
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
  implementation(libs.jgit)
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

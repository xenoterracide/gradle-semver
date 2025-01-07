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
  api(projects.git)

  compileOnlyApi(libs.jspecify)
  api(libs.semver)
  implementation(libs.vavr)
  implementation(libs.guava)
  implementation(libs.commons.lang)
  implementation(libs.java.tools)
  shadow(libs.vavr)
  shadow(libs.semver)
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      dependencies {
        implementation(gradleTestKit())
        implementation(platform(libs.junit.bom))
        implementation(project())
        implementation(testFixtures(projects.git))
        implementation.bundle(libs.bundles.test.impl)
        runtimeOnly.bundle(libs.bundles.test.runtime)
      }
    }
    val test by getting(JvmTestSuite::class) {
      dependencies {
        implementation(libs.maven.artifact)
      }
    }

    val testIntegration by registering(JvmTestSuite::class) {
      dependencies {
      }
    }
  }
}

tasks.check {
  dependsOn(testing.suites.named("testIntegration"))
}

tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set("")
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

// SPDX-FileCopyrightText: Copyright © 2023 - 2026 Caleb Cushing
//
// SPDX-License-Identifier: MIT

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  id("com.xenoterracide.gradle.convention.test")
  `java-gradle-plugin`
}

val libs = the<LibrariesForLibs>()

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      dependencies {
        implementation(gradleTestKit())
        implementation(platform(libs.junit.bom))
        implementation.bundle(libs.bundles.test.impl)
        runtimeOnly.bundle(libs.bundles.test.runtime)
      }
    }

    val testIntegration by registering(JvmTestSuite::class) {
      gradlePlugin.testSourceSet(sources)
      dependencies {
        runtimeOnly(project())
      }
    }
  }
}

tasks.check {
  dependsOn(testing.suites.named("testIntegration"))
}

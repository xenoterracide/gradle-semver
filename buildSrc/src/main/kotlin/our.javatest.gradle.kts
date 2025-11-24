// SPDX-FileCopyrightText: Copyright © 2023 - 2025 Caleb Cushing
//
// SPDX-License-Identifier: MIT

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-gradle-plugin`
  `java-library`
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

    val test by getting(JvmTestSuite::class) {
      dependencies {
        implementation(project())
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

val available =
  tasks.register("tests available") {
    val java: Provider<FileCollection> = sourceSets.test.map { it.java }
    doLast {
      if (java.get().isEmpty) throw RuntimeException("no tests found")
    }
  }

tasks.withType<Test>().configureEach {
  jvmArgs("-XX:+EnableDynamicAgentLoading")
  useJUnitPlatform()
  maxParallelForks =
    Runtime
      .getRuntime()
      .availableProcessors()
      .div(2)
      .or(1)
  systemProperties(
    "junit.jupiter.execution.parallel.enabled" to "true",
    "junit.jupiter.execution.parallel.mode.default" to "concurrent",
    "junit.jupiter.execution.parallel.mode.classes.default" to "concurrent",
  )
  reports {
    junitXml.required.set(false)
    html.required.set(false)
  }
  testLogging {
    lifecycle {
      showStandardStreams = true
      displayGranularity = 2
      exceptionFormat = TestExceptionFormat.FULL
      events.addAll(
        listOf(
          TestLogEvent.SKIPPED,
          TestLogEvent.FAILED,
        ),
      )
    }
  }
  inputs.dir(rootProject.file("buildSrc/src/main"))
  finalizedBy(available)

  afterSuite(
    KotlinClosure2<TestDescriptor, TestResult, Unit>(
      { descriptor, result ->
        if (descriptor.parent == null) {
          logger.lifecycle("Tests run: ${result.testCount}, Failures: ${result.failedTestCount}, Skipped: ${result.skippedTestCount}")
          if (result.testCount == 0L) throw IllegalStateException("You cannot have 0 tests")
        }
        Unit
      },
    ),
  )
}

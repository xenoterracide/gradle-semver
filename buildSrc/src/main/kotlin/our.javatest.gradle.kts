// © Copyright 2023-2024 Caleb Cushing
// SPDX-License-Identifier: MIT

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-library`
  `java-test-fixtures`
}

val libs = the<LibrariesForLibs>()

dependencies {
  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.bundles.test)
  testRuntimeOnly(libs.bundles.junit.platform)
}

val available = tasks.register("tests available") {
  val java: Provider<FileCollection> = sourceSets.test.map { it.java }
  doLast {
    if (java.get().isEmpty) throw RuntimeException("no tests found")
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
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

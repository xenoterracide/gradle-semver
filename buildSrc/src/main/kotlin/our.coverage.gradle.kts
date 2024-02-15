// SPDX-License-Identifier: MIT
// Copyright © 2023-2024 Caleb Cushing.
plugins {
  jacoco
  `java-base`
}

val coverageMinimum: Property<Double> = project.objects.property<Double>()

tasks.withType<JacocoReport> {
  dependsOn(project.tasks.withType<Test>())
}

project.tasks.check.configure {
  dependsOn(tasks.withType<JacocoCoverageVerification>())
}

tasks.withType<JacocoCoverageVerification>().configureEach {
  dependsOn(project.tasks.withType<JacocoReport>())
  violationRules {
    rule {
      limit {
        coverageMinimum.convention(0.9).let { minimum = it.get().toBigDecimal() }
      }
    }
  }
}

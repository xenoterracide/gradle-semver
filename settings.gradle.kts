// SPDX-License-Identifier: MIT
// Copyright © 2024 Caleb Cushing.

rootProject.name = "gradle-semver"

plugins {
  id("com.gradle.enterprise") version ("3.16.2")
}

gradleEnterprise {
  buildScan {
    val isCi = providers.environmentVariable("CI").isPresent

    publishOnFailureIf(isCi)
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

// SPDX-License-Identifier: MIT
// Copyright © 2024 Caleb Cushing.

rootProject.name = "gradle-semver"

plugins {
  id("com.gradle.enterprise") version ("3.16.2")
}

gradleEnterprise {
  buildScan {
    val isCi = providers.environmentVariable("CI").isPresent
    val isCiAndMain = isCi &&
      providers.environmentVariable("GITHUB_REF_PROTECTED").map { it.toBoolean() }
        .getOrElse(false)

    publishAlwaysIf(isCi)
    publishOnFailureIf(isCi)
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

// © Copyright 2023-2024 Caleb Cushing
// SPDX-License-Identifier: MIT

plugins {
  `java-base`
  id("com.diffplug.spotless")
}

val copyright = "// © Copyright \$YEAR Caleb Cushing\n"
val javaLicense = "// SPDX-License-Identifier: Apache-2.0\n\n"
val gradleLicense = "// SPDX-License-Identifier: MIT\n\n"

spotless {
  if (!providers.environmentVariable("CI").isPresent) {
    ratchetFrom("origin/main")
  }

  java {
    licenseHeader(copyright + javaLicense)
  }

  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("**/build/**")
    ktlint().editorConfigOverride(mapOf("ktlint_standard_value-argument-comment" to "disabled"))
    licenseHeader(copyright + gradleLicense, "(import|buildscript|plugins|root)")
  }
}

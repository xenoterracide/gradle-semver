// SPDX-License-Identifier: MIT
// © Copyright 2023-2024 Caleb Cushing. All rights reserved.

plugins {
  `java-base`
  id("com.diffplug.spotless")
}

val copyright = "// © Copyright \$YEAR Caleb Cushing. All rights reserved.\n\n"
val javaLicense = "// SPDX-License-Identifier: Apache-2.0\n"
val gradleLicense = "// SPDX-License-Identifier: MIT\n"

spotless {
  if (!providers.environmentVariable("CI").isPresent) {
    ratchetFrom("origin/main")
  }

  java {
    licenseHeader(javaLicense + copyright)
    cleanthat().addMutators(listOf("SafeAndConsensual", "SafeButNotConsensual"))
  }

  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("**/build/**")
    ktlint().editorConfigOverride(mapOf("ktlint_standard_value-argument-comment" to "disabled"))
    licenseHeader(gradleLicense + copyright, "(import|buildscript|plugins|root)")
  }
}

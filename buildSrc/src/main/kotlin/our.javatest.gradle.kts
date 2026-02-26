// SPDX-FileCopyrightText: Copyright © 2023 - 2026 Caleb Cushing
//
// SPDX-License-Identifier: MIT

plugins {
  id("com.xenoterracide.gradle.convention.test")
  `java-gradle-plugin`
}

tasks.withType<Test>().configureEach {
  // Allow configuration cache when using TestKit
  jvmArgs("-XX:+EnableDynamicAgentLoading")
}

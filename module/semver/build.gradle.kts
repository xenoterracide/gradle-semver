// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: MIT

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  our.javalibrary
  alias(libs.plugins.shadow)
}

group = "com.xenoterracide"

tasks.compileJava {
  options.release = 11
}

dependencyLocking {
  lockAllConfigurations()
}

dependencies {
  compileOnlyApi(libs.jspecify)
  api(libs.jgit)
  api(libs.semver)
  implementation(libs.vavr)
  implementation(libs.guava)
  testImplementation(libs.junit.api)
  testImplementation(libs.maven.artifact)
  testImplementation(gradleTestKit())
  shadow(libs.vavr)
  shadow(libs.semver)
}

tasks.withType<ShadowJar>().configureEach {
  archiveClassifier.set("")
  relocate("org.eclipse.jgit", "com.xenoterracide.gradle.semver.jgit")
  relocate("com.google.common", "com.xenoterracide.gradle.semver.guava")
  dependencies {
    exclude { it.moduleGroup == "io.vavr" }
    exclude { it.moduleGroup == "org.slf4j" }
    exclude { it.moduleName == "semver4j" }
  }
}

// SPDX-License-Identifier: MIT
// Copyright © 2024 Caleb Cushing.
buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  `java-gradle-plugin`
  id("our.javalibrary")
}

version = "0.8.4"
group = "com.xenoterracide"
repositories {
  mavenCentral()
}

dependencyLocking {
  lockAllConfigurations()
}

dependencies {
  implementation(libs.commons.lang)
  implementation(libs.jgit)
  testImplementation(gradleTestKit())
}

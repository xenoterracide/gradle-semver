// SPDX-License-Identifier: MIT
// Copyright © 2024 Caleb Cushing.
plugins {
  `java-library`
  `java-gradle-plugin`
  id("our.spotless")
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
  implementation("org.apache.commons:commons-lang3:3.+")
  implementation("org.eclipse.jgit:org.eclipse.jgit:6.+")
}

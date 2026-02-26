// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: MIT

import com.xenoterracide.gradle.convention.publish.GithubPublicRepositoryConfiguration
import net.ltgt.gradle.errorprone.ErrorProneOptions
import org.gradle.accessors.dm.LibrariesForLibs


plugins {
  id("com.autonomousapps.dependency-analysis")
  id("com.gradle.plugin-publish")
  id("com.xenoterracide.gradle.convention.checkstyle")
  id("com.xenoterracide.gradle.convention.compile")
  id("com.xenoterracide.gradle.convention.coverage")
  id("com.xenoterracide.gradle.convention.publish")
  id("com.xenoterracide.gradle.convention.spotbugs")
  id("com.xenoterracide.gradle.convention.test")
}

val libs = the<LibrariesForLibs>()

dependencies {
  spotbugs(libs.spotbugs)
  errorprone(libs.bundles.ep)
  compileOnly(libs.errorprone.annotations)
}

repositoryHost(GithubPublicRepositoryConfiguration())
repositoryHost.namespace.set("xenoterracide")

gradlePlugin {
  website.set(repositoryHost.repository.websiteUrl.map { it.toString() })
  vcsUrl.set(repositoryHost.repository.cloneUrl.map { it.toString() })
}

publicationLegal {
  inceptionYear.set(2024)
  spdxLicenseIdentifiers.add("GPL-3.0-or-later WITH Classpath-exception-2.0")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(25))
  }
}

// Disable JSpecifyMode for NullAway due to Gradle Provider API not having nullable type parameters
tasks.withType<JavaCompile>().configureEach {
  (options as ExtensionAware).extensions.configure<ErrorProneOptions> {
    option("NullAway:JSpecifyMode", false)
  }
}

// Configure Javadoc to accept custom tags
tasks.withType<Javadoc>().configureEach {
  (options as StandardJavadocDocletOptions).apply {
    addMultilineStringsOption("tag").value =
      listOf(
        "apiSpec:a:API Spec:",
        "apiNote:a:API Note:",
        "implSpec:a:Implementation Spec:",
        "implNote:a:Implementation Note:",
      )
  }
}

// SPDX-FileCopyrightText: Copyright © 2024-2026 Caleb Cushing
//
// SPDX-License-Identifier: MIT

import com.xenoterracide.gradle.convention.publish.GithubPublicRepositoryConfiguration
import net.ltgt.gradle.errorprone.ErrorProneOptions
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.plugins.ExtensionAware


plugins {
  `java-gradle-plugin`
  `java-library`
  id("com.autonomousapps.dependency-analysis")
  id("com.gradle.plugin-publish")
  id("com.xenoterracide.gradle.convention.checkstyle")
  id("com.xenoterracide.gradle.convention.compile")
  id("com.xenoterracide.gradle.convention.coverage")
  id("com.xenoterracide.gradle.convention.javadoc")
  id("com.xenoterracide.gradle.convention.publish")
  id("com.xenoterracide.gradle.convention.spotbugs")
  id("com.xenoterracide.gradle.convention.test")
}

val libs = the<LibrariesForLibs>()

dependencies {
  compileOnly(libs.errorprone.annotations)
  errorprone(libs.bundles.ep)
  spotbugs(libs.spotbugs)
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

tasks.compileJava {
  options.release.set(17)
  // Disable check that doesn't apply to Gradle plugins
  // Gradle uses @Inject on abstract class constructors for DI
  (options as ExtensionAware).extensions.configure<net.ltgt.gradle.errorprone.ErrorProneOptions> {
    disable("InjectOnConstructorOfAbstractClass")
  }
}

// From our.bom.gradle.kts
configurations.configureEach {
  exclude(group = "org.slf4j", module = "slf4j-nop")
  exclude(group = "junit", module = "junit")
  exclude(group = "commons-codec", module = "commons-codec")
  exclude(group = "com.googlecode.javaewah", module = "JavaEWAH")

  resolutionStrategy {
    componentSelection {
      all {
        if (!candidate.group.matches(Regex("^com.xenoterracide.*"))) {
          val nonRelease = Regex("^[\\d.]+-(M|RC|ea|beta|alpha).*$")
          if (candidate.version.matches(nonRelease)) reject("no pre-release")
        }
      }
    }
  }
}

configurations.matching { it.name == "runtimeClasspath" || it.name == "testRuntimeClasspath" }.configureEach {
  exclude(group = "com.google.code.findbugs", module = "jsr305")
  exclude(group = "com.google.errorprone", module = "error_prone_annotations")
  exclude(group = "org.checkerframework", module = "checker-qual")
}

// From our.javatest.gradle.kts
testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      dependencies {
        implementation(gradleTestKit())
        implementation(platform(libs.junit.bom))
        implementation.bundle(libs.bundles.test.impl)
        runtimeOnly.bundle(libs.bundles.test.runtime)
      }
    }

    val testIntegration by registering(JvmTestSuite::class) {
      gradlePlugin.testSourceSet(sources)
      dependencies {
        runtimeOnly(project())
      }
    }
  }
}

tasks.check {
  dependsOn(testing.suites.named("testIntegration"))
}

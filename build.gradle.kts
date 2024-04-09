// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: MIT

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  our.javalibrary
  alias(libs.plugins.dependency.analysis)
  alias(libs.plugins.shadow)
  alias(libs.plugins.gradle.plugin.publish)
  alias(libs.plugins.semver)
}

version = semver.gradlePlugin
group = "com.xenoterracide"

var printVersion = tasks.register("printVersion") {
  println("version: $version")
}

tasks.compileJava {
  options.release = 11
}

repositories {
  mavenCentral()
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
  testImplementation(libs.mockito) { version { require("5.+") } }
  testImplementation(gradleTestKit())
  shadow(libs.vavr)
  shadow(libs.semver)
}

tasks.withType<ShadowJar>().configureEach {
  dependsOn(printVersion)
  archiveClassifier.set("")
  relocate("org.eclipse.jgit", "com.xenoterracide.gradle.semver.jgit")
  relocate("com.google.common", "com.xenoterracide.gradle.semver.guava")
  dependencies {
    exclude { it.moduleGroup == "io.vavr" }
    exclude { it.moduleGroup == "org.slf4j" }
    exclude { it.moduleName == "semver4j" }
  }
}

dependencyAnalysis {
  issues {
    all {
      onAny {
        severity("fail")
      }
      onUnusedDependencies {
        exclude(libs.junit.parameters)
      }
    }
  }
}

val repo = "gradle-semver"
val username = "xenoterracide"
val githubUrl = "https://github.com"
val repoShort = "$username/$repo"
val pub = "pub"

gradlePlugin {
  website.set("$githubUrl/$repoShort")
  vcsUrl.set("${website.get()}.git")
  plugins {
    create(pub) {
      id = "com.xenoterracide.gradle.semver"
      displayName = "Semver with Git"
      description = "A semantic versioning plugin that derives the version from git tags and commits and is configuration cache safe."
      tags = setOf("semver", "versioning", "git")
      implementationClass = "com.xenoterracide.gradle.semver.SemverPlugin"
    }
  }
}

publishing {
  publications {
    withType<MavenPublication>().configureEach {
      versionMapping {
        allVariants {
          fromResolutionResult()
        }
      }
      pom {
        inceptionYear = "2018"
        licenses {
          license {
            name = "Apache-2.0"
            url = "https://choosealicense.com/licenses/apache-2.0/"
            distribution = "repo"
            comments = "Java"
          }
          license {
            name = "MIT"
            url = "https://choosealicense.com/licenses/mit/"
            distribution = "repo"
            comments = "Gradle Build Files and Configuration Files"
          }
          license {
            name = "CC-BY-4.0"
            url = "https://choosealicense.com/licenses/cc-by-4.0/"
            distribution = "repo"
            comments = "Documentation including Javadoc"
          }
        }
        developers {
          developer {
            id = username
            name = "Caleb Cushing"
            email = "xenoterracide@gmail.com"
          }
        }
        scm {
          connection = "$githubUrl/$repoShort.git"
          developerConnection = "scm:git:$githubUrl/$repoShort.git"
          url = "$githubUrl/$repoShort"
        }
      }
    }
  }
}

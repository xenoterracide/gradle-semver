// SPDX-License-Identifier: MIT
// Copyright © 2024 Caleb Cushing.

import com.gradle.scan.agent.serialization.scan.serializer.kryo.id
import com.squareup.kotlinpoet.Dynamic.tags

// SPDX-License-Identifier: MIT
// Copyright © 2024 Caleb Cushing.

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  `java-gradle-plugin`
  id("our.javalibrary")
  alias(libs.plugins.dependency.analysis)
  alias(libs.plugins.gradle.plugin.publish)
}

version = "0.9.0"
group = "com.xenoterracide"
repositories {
  mavenCentral()
}

dependencyLocking {
  lockAllConfigurations()
}

dependencies {
  compileOnlyApi(libs.jspecify)
  implementation(libs.jgit)
  implementation(libs.vavr)
  api(libs.semver)
  testImplementation(libs.junit.api)
  testImplementation(gradleTestKit())
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

val username = "xenoterracide"
val githubUrl = "https://github.com"
val repoShort = "$username/gradle-semver"
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
      implementationClass = "com.xenoterracide.gradle.semver.SemVerPlugin"
    }
  }
}

publishing {
  publications {
    this.named("pluginMaven", MavenPublication::class) {
    versionMapping {
        allVariants {
          fromResolutionResult()
        }
      }
      pom {
        inceptionYear = "2024"
        licenses {
          license {
            name = "Apache-2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            distribution = "repo"
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

  repositories {
    maven {
      name = "GH"
      url = uri("https://maven.pkg.github.com/$repoShort")
      credentials(PasswordCredentials::class)
    }
  }
}

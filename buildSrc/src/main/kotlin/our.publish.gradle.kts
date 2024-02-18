// SPDX-License-Identifier: MIT
// Copyright © 2024 Caleb Cushing.

plugins {
  `maven-publish`
}

val username = "xenoterracide"
val githubUrl = "https://github.com"
val repoShort = "$username/java-commons"

publishing {
  publications {
    create<MavenPublication>("maven") {
      //     val gitVersion: groovy.lang.Closure<String> by extra

      versionMapping {
        allVariants {
          fromResolutionResult()
        }
      }
      pom {
        artifactId = project.name
        groupId = rootProject.group.toString()
//        version = gitVersion()
        description = project.description
        inceptionYear = "2024"
        url = "$githubUrl/$repoShort"
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
      from(components["java"])
    }
  }

  repositories {
    maven {
      name = "GH"
      url = uri("https://maven.pkg.github.com/$repoShort")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
}

// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: MIT

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript { dependencyLocking { lockAllConfigurations() } }

plugins {
  our.javalibrary
  alias(libs.plugins.shadow)
}

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
  implementation(libs.slf4j.api)
  implementation(libs.java.tools)
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
  relocate("com.xenoterracide.tools.java", "com.xenoterracide.tools.java")
  dependencies {
    exclude { it.moduleGroup == "io.vavr" }
    exclude { it.moduleGroup == "org.slf4j" }
    exclude { it.moduleName == "semver4j" }
  }
  minimize()
}

val asciiDoclet by configurations.registering

dependencies {
  asciiDoclet("org.asciidoctor:asciidoclet:2.+")
}

tasks.withType<Javadoc>().configureEach {
  options {
    docletpath(*asciiDoclet.get().files.toTypedArray())
    doclet("org.asciidoctor.asciidoclet.Asciidoclet")
    overview("README.adoc")
    jFlags(
      "--add-exports=jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
      "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
      "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
    )
  }
  (options as StandardJavadocDocletOptions).apply {
    addStringOption("-base-dir", project.layout.projectDirectory.asFile.path)
    addStringsOption("-attribute", ",").value = listOf("name=${project.name}", "version=${project.version}")
    addMultilineStringsOption("tag").value =
      listOf(
        "apiSpec:a:API Spec:",
        "apiNote:a:API Note:",
        "implSpec:a:Implementation Spec:",
        "implNote:a:Implementation Note:",
      )
  }
}

gradlePlugin {
  plugins {
    register("com.xenoterracide.gradle.semver") {
      displayName = "Semver with Git"
      implementationClass = "com.xenoterracide.gradle.semver.SemverPlugin"
      description =
        """
               A semantic versioning plugin that derives the version from git
        tags and commits and is configuration cache safe.
        """.trimIndent()
      tags = setOf("semver", "versioning", "git", "version")
      id = name
    }
  }
}

publishing {
  publications {
    register<MavenPublication>("relocation") {
      pom {
        groupId = "com.xenoterracide"
        artifactId = project.name
        version = rootProject.version.toString()

        distributionManagement {
          relocation {
            groupId = rootProject.group.toString()
            artifactId = project.name
            version = rootProject.version.toString()
            message = "groupId has been changed to follow my conventions"
          }
        }
      }
    }
  }
}

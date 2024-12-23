// © Copyright 2024 Caleb Cushing
// SPDX-License-Identifier: MIT
import com.xenoterracide.gradle.convention.publish.GithubPublicRepositoryConfiguration


plugins {
  id("com.autonomousapps.dependency-analysis")
  id("com.gradle.plugin-publish")
  id("com.xenoterracide.gradle.convention.coverage")
  id("com.xenoterracide.gradle.convention.publish")
}

repositoryHost(GithubPublicRepositoryConfiguration())
repositoryHost.namespace.set("xenoterracide")

gradlePlugin {
  website.set(repositoryHost.repository.wesiteUrl.map { it.toString() })
  vcsUrl.set(repositoryHost.repository.cloneUrl.map { it.toString() })
}

publicationLegal {
  inceptionYear.set(2024)
  spdxLicenseIdentifiers.add("Apache-2.0")
}

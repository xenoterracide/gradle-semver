plugins {
    `java-library`
    `java-gradle-plugin`
}

version = "0.8.4"
group="com.xenoterracide"
repositories {
    mavenCentral()
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.+")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.+")
}

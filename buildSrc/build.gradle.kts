plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.diffplug.spotless") version "5.12.4"
}
buildscript {
    dependencyLocking {
        lockAllConfigurations()
    }
}
dependencyLocking {
    lockAllConfigurations()
}
repositories {
    mavenCentral()
}
spotless {
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("0.40.0")
    }
}

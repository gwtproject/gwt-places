plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.diffplug.spotless") version "5.10.2"
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
kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
spotless {
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("0.40.0")
    }
}

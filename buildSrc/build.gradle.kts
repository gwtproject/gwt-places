plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
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
    jcenter()
}
kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
ktlint {
    version.set("0.36.0")
    enableExperimentalRules.set(true)
    filter {
        exclude {
            it.file in fileTree(buildDir)
        }
    }
}

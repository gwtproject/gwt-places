plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
}
repositories {
    jcenter()
}
kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
ktlint {
    version.set("0.34.2")
    enableExperimentalRules.set(true)
    filter {
        exclude {
            it.file in fileTree(buildDir)
        }
    }
}

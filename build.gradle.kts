plugins {
    id("local.java-library")
    id("local.maven-publish")
    id("net.ltgt.errorprone") version "1.3.0"
    id("com.diffplug.spotless") version "5.10.2"
}
buildscript {
    dependencyLocking {
        lockAllConfigurations()
        lockMode.set(LockMode.STRICT)
    }
}
allprojects {
    dependencyLocking {
        lockAllConfigurations()
        lockMode.set(LockMode.STRICT)
    }
}
tasks {
    register("allDependencies") {
        dependsOn("dependencies", subprojects.map { ":${it.name}:dependencies" })
    }
}

apply(from = "gwtTest.gradle.kts")

group = "org.gwtproject.place"

repositories {
    mavenCentral()
}

dependencies {
    api("org.gwtproject.event:gwt-event:1.0.0-RC1")
    api("org.gwtproject.event:gwt-logical-event:1.0.0-RC1")
    implementation("org.gwtproject.user.history:gwt-history:1.0.0-RC1")
    implementation("org.gwtproject.user.window:gwt-window:1.0.0-RC2")

    testImplementation("junit:junit:4.13.2")
}

val jar by tasks.getting(Jar::class) {
    from(sourceSets.main.map { it.allJava })
}

allprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        java {
            targetExclude(fileTree(buildDir))
            googleJavaFormat("1.7")
            licenseHeaderFile(rootProject.file("LICENSE.header"))
        }
        kotlinGradle {
            ktlint("0.40.0")
        }
    }
}

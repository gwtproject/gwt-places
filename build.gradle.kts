import java.time.Year

plugins {
    id("local.java-library")
    id("local.maven-publish")
    id("net.ltgt.errorprone") version "1.1.1"
    id("com.github.sherter.google-java-format") version "0.8"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("com.github.hierynomus.license") version "0.15.0"
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
    implementation("org.gwtproject.user.window:gwt-window:1.0.0-RC1")

    testImplementation("junit:junit:4.13")
}

val jar by tasks.getting(Jar::class) {
    from(sourceSets.main.map { it.allJava })
}

googleJavaFormat {
    toolVersion = "1.7"
}
allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set("0.36.0")
        enableExperimentalRules.set(true)
    }
}

license {
    header = rootProject.file("LICENSE.header")
    encoding = "UTF-8"
    skipExistingHeaders = true
    mapping("java", "SLASHSTAR_STYLE")

    (this as ExtensionAware).extra["year"] = Year.now()
    (this as ExtensionAware).extra["name"] = "The GWT Project Authors"
}

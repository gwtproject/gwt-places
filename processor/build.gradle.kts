plugins {
    id("local.java-library")
    id("local.maven-publish")
}
apply(from = "funcTest.gradle.kts")
apply(from = "gwtTest.gradle.kts")

base.archivesBaseName = "gwt-places-processor"

// src/testSupport/java will be compiled twiced: once with annotation processing, and once without
sourceSets {
    getByName("test") {
        java {
            srcDir("src/testSupport/java")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc7")
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc7")

    compileOnly("net.ltgt.gradle.incap:incap:0.3")
    annotationProcessor("net.ltgt.gradle.incap:incap-processor:0.3")

    api("com.google.auto:auto-common:0.10")
    api("com.google.guava:guava:29.0-jre")
    implementation(project(":")) {
        isTransitive = false
    }
    implementation("com.squareup:javapoet:1.12.1")

    testImplementation("junit:junit:4.13")
    testImplementation("com.google.testing.compile:compile-testing:0.18")
    testImplementation("org.mockito:mockito-core:3.3.3")
}

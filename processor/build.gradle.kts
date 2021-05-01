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
    compileOnly("com.google.auto.service:auto-service-annotations:1.0")
    annotationProcessor("com.google.auto.service:auto-service:1.0")

    compileOnly("net.ltgt.gradle.incap:incap:0.3")
    annotationProcessor("net.ltgt.gradle.incap:incap-processor:0.3")

    api("com.google.auto:auto-common:1.0")
    api("com.google.guava:guava:30.1.1-jre")
    implementation(project(":")) {
        isTransitive = false
    }
    implementation("com.squareup:javapoet:1.13.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.testing.compile:compile-testing:0.19")
    testImplementation("org.mockito:mockito-core:3.9.0")
}

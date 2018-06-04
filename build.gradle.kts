plugins {
    id("local.java-library")
    id("net.ltgt.errorprone") version "0.0.14"
    id("com.github.sherter.google-java-format") version "0.6"
    id("local.ktlint")
}
apply(from = "gwtTest.gradle.kts")

group = "org.gwtproject.place"
version = "HEAD-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    api("org.gwtproject.event:gwt-event:HEAD-SNAPSHOT")
    api("org.gwtproject.event:gwt-logical-event:HEAD-SNAPSHOT")
    implementation("org.gwtproject.user.history:gwt-history:HEAD-SNAPSHOT")
    implementation("org.gwtproject.user.window:gwt-window:HEAD-SNAPSHOT")

    testImplementation("junit:junit:4.12")
}

val jar by tasks.getting(Jar::class) {
    from(java.sourceSets["main"].allJava)
}

googleJavaFormat {
    toolVersion = "1.6"
}

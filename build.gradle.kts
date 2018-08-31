import java.time.Year

plugins {
    id("local.java-library")
    id("local.maven-publish")
    id("net.ltgt.errorprone-javacplugin") version "0.5"
    id("com.github.sherter.google-java-format") version "0.7.1"
    id("com.github.hierynomus.license") version "0.14.0"
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

license {
    header = rootProject.file("LICENSE.header")
    encoding = "UTF-8"
    skipExistingHeaders = true
    mapping("java", "SLASHSTAR_STYLE")

    (this as ExtensionAware).extra["year"] = Year.now()
    (this as ExtensionAware).extra["name"] = "The GWT Project Authors"
}

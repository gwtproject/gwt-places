package local

plugins {
    `java-library`
}

apply(plugin = "net.ltgt.errorprone")

dependencies {
    "errorprone"("com.google.errorprone:error_prone_core:2.3.4")
    "errorproneJavac"("com.google.errorprone:javac:9+181-r4173-1")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(arrayOf("-Werror", "-Xlint:all,-processing"))
    if (JavaVersion.current().isJava9Compatible) {
        options.compilerArgs.addAll(arrayOf("--release", java.targetCompatibility.majorVersion))
    }
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as CoreJavadocOptions).apply {
        addBooleanOption("Xdoclint:all,-missing", true)
        if (JavaVersion.current().isJava9Compatible) {
            addBooleanOption("html5", true)
        }
        // Workaround for https://github.com/gradle/gradle/issues/5630
        addStringOption("sourcepath", "")
    }
}

val Project.java: JavaPluginExtension
    get() = the()

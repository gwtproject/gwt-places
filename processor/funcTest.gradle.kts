val funcTestSourceSet = java.sourceSets.create("funcTest") {
    java {
        srcDir("src/testSupport/java")
    }
}

val funcTest by tasks.creating(Test::class) {
    description = "Runs the integration tests"
    group = "verification"
    testClassesDirs = funcTestSourceSet.output.classesDirs
    classpath = funcTestSourceSet.runtimeClasspath

    mustRunAfter(tasks["test"])
}
tasks["compileFuncTestJava"].mustRunAfter(tasks["test"])
tasks["check"].dependsOn(funcTest)

dependencies {
    "funcTestImplementation"("junit:junit:4.12")
    "funcTestImplementation"(project(":"))
    "funcTestAnnotationProcessor"(files(tasks["jar"]))
    "funcTestAnnotationProcessor"(configurations["runtimeClasspath"])
}

inline val Project.java: JavaPluginConvention
    get() = the()

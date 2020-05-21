val funcTestSourceSet = sourceSets.create("funcTest") {
    java {
        srcDir("src/testSupport/java")
    }
}

tasks {
    val funcTest by tasks.registering(Test::class) {
        description = "Runs the integration tests"
        group = "verification"
        testClassesDirs = funcTestSourceSet.output.classesDirs
        classpath = funcTestSourceSet.runtimeClasspath

        mustRunAfter(tasks["test"])
    }
    "compileFuncTestJava" { mustRunAfter(tasks["test"]) }
    "check" { dependsOn(funcTest) }
}

dependencies {
    "funcTestImplementation"("junit:junit:4.13")
    "funcTestImplementation"(project(":"))
    "funcTestAnnotationProcessor"(files(tasks["jar"]))
    "funcTestAnnotationProcessor"(configurations["runtimeClasspath"])
}

inline val Project.sourceSets: SourceSetContainer
    get() = the()

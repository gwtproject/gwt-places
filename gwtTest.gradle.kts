val generateGwtTestSources by tasks.registering(Copy::class) {
    from(sourceSets["test"].allJava.sourceDirectories)
    into("$buildDir/generated/source/gwtTest/")
    filter {
        if (it == "import junit.framework.TestCase;") {
            "import com.google.gwt.junit.client.GWTTestCase;"
        } else {
            it.replace(
                "extends TestCase {",
                """extends GWTTestCase {
  @Override
  public String getModuleName() { return "org.gwtproject.place.Place"; }"""
            )
                .replace("protected void setUp()", "protected void gwtSetUp()")
                .replace("super.setUp()", "super.gwtSetUp()")
                .replace("protected void tearDown()", "protected void gwtTearDown()")
                .replace("super.tearDown()", "super.gwtTearDown()")
        }
    }

    mustRunAfter(tasks["compileTestJava"])
}

sourceSets {
    create("gwtTest") {
        java {
            srcDirs(files(generateGwtTestSources.map { it.destinationDir }))
        }
        compileClasspath += sourceSets["test"].compileClasspath
        runtimeClasspath += output + sourceSets["test"].runtimeClasspath + sourceSets["main"].allJava.sourceDirectories + sourceSets["gwtTest"].allJava.sourceDirectories
    }
}
dependencies {
    "gwtTestImplementation"("com.google.gwt:gwt-user:2.9.0")

    "gwtTestRuntimeOnly"("com.google.gwt:gwt-dev:2.9.0")
}

tasks {
    val gwtTest by registering(Test::class) {
        val warDir = file("$buildDir/gwt/www-test")
        val workDir = file("$buildDir/gwt/work")
        val cacheDir = file("$buildDir/gwt/cache")
        doFirst {
            mkdir(warDir)
            mkdir(workDir)
            mkdir(cacheDir)
        }

        testClassesDirs = sourceSets["gwtTest"].output.classesDirs
        classpath = sourceSets["gwtTest"].runtimeClasspath
        include("**/*Suite.class")
        systemProperty("gwt.args", "-ea -draftCompile -batch module -war \"$warDir\" -workDir \"$workDir\"")
        systemProperty("gwt.persistentunitcachedir", cacheDir)

        mustRunAfter(tasks["test"])
    }
    "check" { dependsOn(gwtTest) }
}

inline val Project.sourceSets: SourceSetContainer
    get() = the()

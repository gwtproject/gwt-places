val generateGwtTestSources by tasks.creating(Copy::class) {
    from("src/funcTest/java")
    into("$buildDir/generated/source/gwtTest/")
    filter {
        if (it == "import junit.framework.TestCase;") {
            "import com.google.gwt.junit.client.GWTTestCase;"
        } else {
            it.replace(
                "extends TestCase {", """extends GWTTestCase {
  @Override
  public String getModuleName() { return "org.gwtproject.place.PlaceSuite"; }"""
            )
                .replace("protected void setUp()", "protected void gwtSetUp()")
                .replace("super.setUp()", "super.gwtSetUp()")
                .replace("protected void tearDown()", "protected void gwtTearDown()")
                .replace("super.tearDown()", "super.gwtTearDown()")
        }
    }

    mustRunAfter(tasks["compileFuncTestJava"])
}

sourceSets {
    create("gwtTest") {
        java {
            srcDirs(files(generateGwtTestSources.destinationDir).builtBy(generateGwtTestSources))
            srcDir("src/testSupport/java")
        }
        compileClasspath += sourceSets["funcTest"].compileClasspath
        runtimeClasspath += output + compileClasspath +
            sourceSets["main"].allJava.sourceDirectories +
            sourceSets["gwtTest"].allJava.sourceDirectories +
            files((tasks["compileGwtTestJava"] as JavaCompile).options.annotationProcessorGeneratedSourcesDirectory)
    }
}
configurations {
    getByName("gwtTestCompile").extendsFrom(getByName("funcTestCompile"))
    getByName("gwtTestImplementation").extendsFrom(getByName("funcTestImplementation"))
    getByName("gwtTestAnnotationProcessor").extendsFrom(getByName("funcTestAnnotationProcessor"))
    getByName("gwtTestRuntime").extendsFrom(getByName("funcTestRuntime"))
    getByName("gwtTestRuntimeOnly").extendsFrom(getByName("funcTestRuntimeOnly"))
}
dependencies {
    "gwtTestImplementation"("com.google.gwt:gwt-user:2.8.2")

    "gwtTestRuntimeOnly"("com.google.gwt:gwt-dev:2.8.2")
}

val gwtTest by tasks.creating(Test::class) {
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

    mustRunAfter(tasks["funcTest"])
}
tasks["check"].dependsOn(gwtTest)

inline val Project.sourceSets: SourceSetContainer
    get() = the()

val generateGwtTestSources by tasks.creating(Copy::class) {
    from(sourceSets["test"].allJava.sourceDirectories)
    into("$buildDir/generated/source/gwtTest/")
    filter {
        if (it == "import junit.framework.TestCase;") {
            "import com.google.gwt.junit.client.GWTTestCase;"
        } else {
            it.replace(
                "extends TestCase {", """extends GWTTestCase {
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
            srcDirs(files(generateGwtTestSources.destinationDir).builtBy(generateGwtTestSources))
        }
        compileClasspath += sourceSets["test"].compileClasspath
        runtimeClasspath += output + compileClasspath + sourceSets["main"].allJava.sourceDirectories + sourceSets["gwtTest"].allJava.sourceDirectories
    }
}
configurations {
    findByName("funcTestCompile")?.let { getByName("gwtTestCompile").extendsFrom(it) }
    findByName("funcTestImplementation")?.let { getByName("gwtTestImplementation").extendsFrom(it) }
    findByName("funcTestRuntime")?.let { getByName("gwtTestRuntime").extendsFrom(it) }
    findByName("funcTestRuntimeOnly")?.let { getByName("gwtTestRuntimeOnly").extendsFrom(it) }
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

    mustRunAfter(tasks["test"])
}
tasks["check"].dependsOn(gwtTest)

inline val Project.sourceSets: SourceSetContainer
    get() = the()

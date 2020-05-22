package local

plugins {
    `java-base`
    `maven-publish`
    signing
}

if (project != rootProject) {
    group = rootProject.group
}

java {
    withJavadocJar()
    withSourcesJar()
}

val sonatypeRepository = publishing.repositories.maven {
    name = "sonatype"
    setUrl(
        provider {
            if (isSnapshot) {
                uri("https://oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
        }
    )
    credentials {
        username = project.findProperty("ossrhUsername") as? String
        password = project.findProperty("ossrhPassword") as? String
    }
}

val mavenPublication = publishing.publications.create<MavenPublication>("maven") {
    from(components["java"])
    afterEvaluate {
        artifactId = base.archivesBaseName
    }
    if (isSnapshot) {
        version = "HEAD-SNAPSHOT"
    }

    pom {
        name.set(provider { "$groupId:$artifactId" })
        description.set(provider { project.description ?: name.get() })
        url.set("https://github.com/gwtproject/gwt-places")
        developers {
            developer {
                name.set("The GWT Project Authors")
                url.set("http://www.gwtproject.org")
            }
        }
        scm {
            connection.set("https://github.com/gwtproject/gwt-places.git")
            developerConnection.set("scm:git:ssh://github.com:gwtproject/gwt-places.git")
            url.set("https://github.com/gwtproject/gwt-places")
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
    }
}

signing {
    useGpgCmd()
    isRequired = !isSnapshot
    sign(mavenPublication)
}

inline val Project.isSnapshot
    get() = version == Project.DEFAULT_VERSION

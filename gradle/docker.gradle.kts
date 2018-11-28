import com.palantir.gradle.docker.DockerExtension

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("gradle.plugin.com.palantir.gradle.docker:gradle-docker:0.20.1")
    }
}

apply<com.palantir.gradle.docker.PalantirDockerPlugin>()

tasks {
    val dockerPrepare = getByName("dockerPrepare")
    val jar = tasks["jar"]

    dockerPrepare.dependsOn(jar)

    dockerPrepare.doLast {
        project.copy {
            from(configurations.getByName("runtime"))
            into("$buildDir/docker/lib")
        }

        project.copy {
            from(jar as CopySpec)
            into("$buildDir/docker/app/")
        }
    }
}

configure<DockerExtension> {
    name = "bynull-docker-acinonyx.bintray.io/${project.name}:$version"
    setDockerfile(file("Dockerfile"))
    dependsOn(tasks["check"])
}
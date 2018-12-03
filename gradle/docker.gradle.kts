import com.palantir.gradle.docker.DockerExtension

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("gradle.plugin.com.palantir.gradle.docker:gradle-docker:0.20.1")
    }
}

apply<com.palantir.gradle.docker.PalantirDockerPlugin>()

tasks {
    val dockerPrepare = getByName("dockerPrepare")
    val jar = tasks["jar"]
    val dockerPush = getByName("dockerPush")
    val check = getByName("check")
    val build = getByName("build")

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

    dockerPush.dependsOn(check)
    build.dependsOn(dockerPush)
}

configure<DockerExtension> {
    name = "bynull-docker-acinonyx.bintray.io/${project.name}:$version"
    setDockerfile(file("Dockerfile"))
}
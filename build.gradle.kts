import com.palantir.gradle.docker.DockerExtension
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaModule

plugins {
    idea
    scala
    id("com.palantir.docker") version "0.20.1"
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
}

repositories {
    mavenCentral()
    jcenter()
}

version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configure<IdeaModel> {
    project {
        languageLevel = IdeaLanguageLevel(JavaVersion.VERSION_1_8)
    }
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

tasks.withType<Test> {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

docker {
    name = "acinonyx:${version}"
    setDockerfile(file("Dockerfile"))
    dependsOn(tasks["check"])
}

tasks.getByName("docker").doFirst {
    project.copy {
        from(configurations.runtime)
        into("$buildDir/docker/lib")
    }

    project.copy {
        from(tasks["jar"])
        into("$buildDir/docker/app")
    }
}

dependencies {
    compile("org.scala-lang:scala-library:2.12.7")

    compile("ch.qos.logback:logback-classic:1.3.0-alpha4")
    compile("com.typesafe.scala-logging:scala-logging_2.12:3.9.0")

    compile("com.github.finagle:finch-core_2.12:0.24.0")
    compile("com.github.finagle:finch-circe_2.12:0.24.0")
    compile("io.circe:circe-generic_2.12:0.9.0")

    testCompile("org.scalatest:scalatest_2.12:3.0.0")
    testCompile("junit:junit:4.12")
}



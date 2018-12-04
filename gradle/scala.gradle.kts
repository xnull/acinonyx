import net.researchgate.release.GitAdapter
import net.researchgate.release.GitAdapter.GitConfig
import net.researchgate.release.ReleaseExtension
import net.researchgate.release.ReleasePlugin
import java.nio.charset.Charset

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("net.researchgate:gradle-release:2.7.0")
    }
}

apply<ScalaPlugin>()
apply<ReleasePlugin>()

version = project.file("version")
        .readText(Charset.defaultCharset())
        .trim()
        .substring("version=".length)

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

group = "acinonyx"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    add("compile", "org.scala-lang:scala-library:2.12.7")

    add("compile", "ch.qos.logback:logback-classic:1.3.0-alpha4")
    add("compile", "com.typesafe.scala-logging:scala-logging_2.12:3.9.0")

    add("compile", "org.typelevel:cats-effect_2.12:1.0.0")
    add("compile", "io.monix:monix_2.12:3.0.0-RC1")

    add("compile", "com.github.finagle:finchx-core_2.12:0.25.0")
    add("compile", "com.github.finagle:finch-circe_2.12:0.25.0")
    add("compile", "com.twitter:bijection-util_2.12:0.9.6")

    add("compile", "io.circe:circe-core_2.12:0.10.1")
    add("compile", "io.circe:circe-generic_2.12:0.10.1")
    add("compile", "io.circe:circe-parser_2.12:0.10.1")
    add("compile", "io.circe:circe-java8_2.12:0.10.1")

    add("compile", "org.tpolecat:doobie-core_2.12:0.6.0")
    add("compile", "com.h2database:h2:1.4.197")

    add("testCompile", "org.scalatest:scalatest_2.12:3.0.0")
    add("testCompile", "junit:junit:4.12")
}

tasks.withType<Test> {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

tasks.withType<Jar> {
    archiveName = "${project.name}.${extension}"
    manifest {
        attributes["Main-Class"] = "acinonyx.Main"
    }
}

configure<ReleaseExtension> {
    tagTemplate = "$name-$version"
    buildTasks = emptyList()
    versionPropertyFile = "version"

    preCommitText = ""
    newVersionCommitMessage = "release: "

    val git: GitConfig = getProperty("git") as GitConfig

    git.requireBranch = "master"
}

tasks.create("getVersion").doLast {
    print(version)
}
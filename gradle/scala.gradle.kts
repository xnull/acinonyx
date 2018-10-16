apply<ScalaPlugin>()

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

    add("compile", "com.github.finagle:finch-core_2.12:0.24.0")
    add("compile", "com.github.finagle:finch-circe_2.12:0.24.0")
    add("compile", "io.circe:circe-generic_2.12:0.9.0")

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
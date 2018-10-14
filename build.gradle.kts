plugins {
    id("scala")
}

buildscript {
    repositories {
        gradlePluginPortal()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    jcenter()
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



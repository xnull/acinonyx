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

    compile("io.netty:netty-all:4.1.30.Final")
    compile("io.netty:netty-tcnative-boringssl-static:2.0.17.Final")
    compile("io.netty:netty-tcnative:2.0.17.Final")

    testCompile("org.scalatest:scalatest_2.12:3.0.0")
    testCompile("junit:junit:4.12")
}

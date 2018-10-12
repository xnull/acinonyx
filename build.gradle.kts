plugins {
    id("scala")
}

buildscript {
    repositories {
        gradlePluginPortal()
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compile("org.scala-lang:scala-library:2.12.7")
    testCompile("org.scalatest:scalatest_2.12:3.0.0")
    testCompile("junit:junit:4.12")
}



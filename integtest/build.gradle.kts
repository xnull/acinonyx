version = "1.0.0"

apply {
    from("$rootDir/gradle/scala.gradle.kts")
}

plugins {
    java
}

dependencies {
    compile(project(":acinonyx-client"))
    compile(project(":acinonyx-server"))

    compile("com.spotify:docker-client:8.14.1")
}
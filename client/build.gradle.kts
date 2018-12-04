apply{
    from("$rootDir/gradle/scala.gradle.kts")
}

apply{
    from("$rootDir/gradle/docker.gradle.kts")
}

dependencies {
    add("compile", project(":acinonyx-api"))
}
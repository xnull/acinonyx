version = "1.0.0"

apply{
    from("$rootDir/gradle/scala.gradle.kts")
}

apply{
    from("$rootDir/gradle/docker.gradle.kts")
}
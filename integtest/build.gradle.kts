apply {
    from("$rootDir/gradle/scala.gradle.kts")
}

dependencies {
    add("compile", project(":acinonyx-client"))
    add("compile", project(":acinonyx-server"))

    add("compile", "com.spotify:docker-client:8.14.1")
}

tasks.withType<Test> {
    dependsOn(project(":acinonyx-client").tasks["docker"])
    dependsOn(project(":acinonyx-server").tasks["docker"])
}
import java.nio.file.Paths

apply {
    from("$rootDir/gradle/idea.gradle.kts")
}

tasks.create("terraform", Exec::class) {
    workingDir = Paths.get(rootDir.absolutePath, "script", "terraform").toFile()

    commandLine = listOf("sh", "-c", "docker run --rm hashicorp/terraform:light -version")
}


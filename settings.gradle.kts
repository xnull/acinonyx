rootProject.name = "acinonyx"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

include("client", "server")

project(":client").name = "acinonix-client"
project(":server").name = "acinonix-server"
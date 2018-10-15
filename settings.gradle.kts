rootProject.name = "acinonyx"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

include("client", "server", "integtest")

project(":client").name = "acinonix-client"
project(":server").name = "acinonix-server"
rootProject.name = "acinonyx"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

include("client", "server", "integtest")

project(":client").name = "acinonyx-client"
project(":server").name = "acinonyx-server"
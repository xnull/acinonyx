rootProject.name = "acinonyx"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

include("client", "server", "api", "integtest")

project(":client").name = "acinonyx-client"
project(":server").name = "acinonyx-server"
project(":api").name = "acinonyx-api"
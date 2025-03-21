rootProject.name = "radium"

pluginManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        maven("https://maven.ornithemc.net/releases")
        gradlePluginPortal()
    }
}

include("common")
include("fabric")
include("ornithe")
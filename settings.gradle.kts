rootProject.name = "radium"

pluginManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        gradlePluginPortal()
    }
}

include("lwjgl3")
include("common")
include("fabric")

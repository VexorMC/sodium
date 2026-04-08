plugins {
    id("multiloader-base")
    id("net.fabricmc.fabric-loom-remap")
    id("ploceus")
}

base {
    archivesName = "radium-common"
}

val configurationPreLaunch = configurations.create("preLaunchDeps") {
    isCanBeResolved = true
}

ploceus {
    setIntermediaryGeneration(2)
}

sourceSets {
    val main = getByName("main")

    main.apply {
        java {
            srcDirs("src/api/java")
        }
    }

    create("desktop")
}

repositories {
    maven("https://jitpack.io/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.MINECRAFT_VERSION}")
    mappings("net.legacyfabric:legacy-yarn:1.8.9+build.4:v2")
    modImplementation("io.github.moehreag:legacy-lwjgl3:1.2.11+${BuildConfig.MINECRAFT_VERSION}")

    implementation("org.joml:joml:1.10.8")

    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")

    compileOnly("net.fabricmc:sponge-mixin:0.13.2+mixin.0.8.5")
    compileOnly("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")

    // We need to be careful during pre-launch that we don't touch any Minecraft classes, since other mods
    // will not yet have an opportunity to apply transformations.
    configurationPreLaunch("net.java.dev.jna:jna:5.14.0")
    configurationPreLaunch("net.java.dev.jna:jna-platform:5.14.0")
    configurationPreLaunch("org.slf4j:slf4j-api:2.0.9")
    configurationPreLaunch("org.jetbrains:annotations:25.0.0")
}

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl") // LWJGL is provided by legacy-lwjgl3
}

loom {
    accessWidenerPath = file("src/main/resources/sodium-common.accesswidener")

    mixin.useLegacyMixinAp = false
}

fun exportSourceSetJava(name: String, sourceSet: SourceSet) {
    val configuration = configurations.create("${name}Java") {
        isCanBeResolved = true
        isCanBeConsumed = true
    }

    val compileTask = tasks.getByName<JavaCompile>(sourceSet.compileJavaTaskName)
    artifacts.add(configuration.name, compileTask.destinationDirectory) {
        builtBy(compileTask)
    }
}

fun exportSourceSetResources(name: String, sourceSet: SourceSet) {
    val configuration = configurations.create("${name}Resources") {
        isCanBeResolved = true
        isCanBeConsumed = true
    }

    val compileTask = tasks.getByName<ProcessResources>(sourceSet.processResourcesTaskName)
    compileTask.apply {
        exclude("**/README.txt")
        exclude("/*.accesswidener")
    }

    artifacts.add(configuration.name, compileTask.destinationDir) {
        builtBy(compileTask)
    }
}

// Exports the compiled output of the source set to the named configuration.
fun exportSourceSet(name: String, sourceSet: SourceSet) {
    exportSourceSetJava(name, sourceSet)
    exportSourceSetResources(name, sourceSet)
}

exportSourceSet("commonMain", sourceSets["main"])
exportSourceSet("commonDesktop", sourceSets["desktop"])

tasks.jar { enabled = false }
tasks.remapJar { enabled = false }
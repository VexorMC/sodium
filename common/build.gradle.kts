plugins {
    id("multiloader-base")
    id("java-library")

    id("fabric-loom") version ("1.8.9")
}

base {
    archivesName = "sodium-common"
}

val configurationPreLaunch = configurations.create("preLaunchDeps") {
    isCanBeResolved = true
}

sourceSets {
    val main = getByName("main")

    main.apply {
        java {
            srcDirs("src/api/java")
            srcDirs("src/vendored/java")
            srcDirs("src/shaders/java")
        }
        resources {
            srcDirs("src/shaders/resources")
        }
    }

    create("desktop")
}

repositories {
    maven("https://jitpack.io/")
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = BuildConfig.MINECRAFT_VERSION)
    mappings("net.legacyfabric:yarn:1.8.9+build.551:v2")

    implementation("org.joml:joml:1.10.8")
    implementation("it.unimi.dsi:fastutil:8.5.15")

    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")

    compileOnly("net.fabricmc:sponge-mixin:0.13.2+mixin.0.8.5")
    compileOnly("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")

    modCompileOnly("net.legacyfabric.legacy-fabric-api:legacy-fabric-api:1.9.4+1.8.9")

    implementation("org.javassist:javassist:3.29.2-GA")
    implementation(platform("org.lwjgl:lwjgl-bom:3.3.5"))
    implementation("org.anarres:jcpp:1.4.14")
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-openal")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-stb")
    implementation("org.lwjgl:lwjgl-tinyfd")
    implementation("io.github.douira:glsl-transformer:1.0.1")
    arrayOf("linux", "windows", "macos", "windows-arm64", "macos-arm64").forEach { platform ->
        runtimeOnly("org.lwjgl:lwjgl::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-glfw::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-openal::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-opengl::natives-$platform")
    }

    // We need to be careful during pre-launch that we don't touch any Minecraft classes, since other mods
    // will not yet have an opportunity to apply transformations.
    configurationPreLaunch("net.java.dev.jna:jna:5.14.0")
    configurationPreLaunch("net.java.dev.jna:jna-platform:5.14.0")
    configurationPreLaunch("org.slf4j:slf4j-api:2.0.9")
    configurationPreLaunch("org.jetbrains:annotations:25.0.0")
}

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl")
}

loom {
    accessWidenerPath = file("src/main/resources/sodium-common.accesswidener")

    mixin {
        useLegacyMixinAp = false
    }
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
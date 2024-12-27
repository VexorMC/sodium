plugins {
    id("multiloader-platform")

    id("fabric-loom") version ("1.8.9")
}

base {
    archivesName = "sodium-fabric"
}

val configurationCommonModJava: Configuration = configurations.create("commonJava") {
    isCanBeResolved = true
}
val shadow: Configuration = configurations.create("shadow") {
    isCanBeResolved = true
}
val configurationCommonModResources: Configuration = configurations.create("commonResources") {
    isCanBeResolved = true
}

repositories {
    maven("https://jitpack.io/")
}

dependencies {
    configurationCommonModJava(project(path = ":common", configuration = "commonMainJava"))
    configurationCommonModResources(project(path = ":common", configuration = "commonMainResources"))

    shadow("org.joml:joml:1.10.8")
    shadow("it.unimi.dsi:fastutil:8.5.15")
}

sourceSets.apply {
    main {
        compileClasspath += configurationCommonModJava
        runtimeClasspath += configurationCommonModJava
        compileClasspath += shadow
        runtimeClasspath += shadow
    }
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = BuildConfig.MINECRAFT_VERSION)
    mappings("net.legacyfabric:yarn:1.8.9+build.551:v2")
    implementation("io.waterwave.Legacy-LWJGL3:lwjgl:3.3.2-5")
    runtimeOnly("org.lwjgl", "lwjgl", version = "3.3.2", classifier = "natives-windows")

    modImplementation("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")
    modImplementation("net.legacyfabric.legacy-fabric-api:legacy-fabric-api:1.9.4+1.8.9")
}

loom {
    accessWidenerPath.set(file("src/main/resources/sodium-fabric.accesswidener"))

    mixin {
        useLegacyMixinAp = false
    }

    runs {
        named("client") {
            client()
            configName = "Fabric/Client"
            appendProjectPathToConfigName = false
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

tasks {
    jar {
        from(configurationCommonModJava)

        shadow.forEach { from(zipTree(it)) { exclude("META-INF", "META-INF/**") } }
    }

    remapJar {
        destinationDirectory.set(file(rootProject.layout.buildDirectory).resolve("mods"))
    }

    processResources {
        from(configurationCommonModResources)
    }
}
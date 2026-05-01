plugins {
    id("multiloader-platform")
    id("net.fabricmc.fabric-loom-remap")
    id("ploceus")
}

base {
    archivesName = "radium-fabric"
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

ploceus {
    setIntermediaryGeneration(2)
}

repositories {
    maven("https://jitpack.io/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    configurationCommonModJava(project(path = ":common", configuration = "commonMainJava"))
    configurationCommonModResources(project(path = ":common", configuration = "commonMainResources"))

    shadow("org.joml:joml:1.10.8")
    shadow("org.jetbrains:annotations:26.0.2")
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
    minecraft("com.mojang:minecraft:${BuildConfig.MINECRAFT_VERSION}")
    mappings("net.legacyfabric:legacy-yarn:1.8.9+build.4:v2")

    modImplementation("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")
    modImplementation("io.github.moehreag:legacy-lwjgl3:1.2.11+${BuildConfig.MINECRAFT_VERSION}")

    ploceus.dependOsl("0.17.1")
}

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl") // LWJGL is provided by legacy-lwjgl3
}

loom {
    accessWidenerPath.set(file("src/main/resources/sodium-fabric.accesswidener"))

    mixin.useLegacyMixinAp = false

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

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    remapJar {
        destinationDirectory.set(file(rootProject.layout.buildDirectory).resolve("mods"))
    }

    processResources {
        from(configurationCommonModResources)
    }
}
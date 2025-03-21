plugins {
    id("multiloader-platform")

    id("fabric-loom") version ("1.8.9")
    id("ploceus") version ("1.8.9")
}

base {
    archivesName = "sodium-ornithe"
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
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    configurationCommonModJava(project(path = ":common", configuration = "commonMainJava"))
    configurationCommonModResources(project(path = ":common", configuration = "commonMainResources"))

    shadow("org.joml:joml:1.10.8")
    shadow("it.unimi.dsi:fastutil:8.5.15")
    shadow("org.lwjgl", "lwjgl", version = "3.3.2", classifier = "natives-linux")
    shadow("org.lwjgl", "lwjgl", version = "3.3.2", classifier = "natives-windows")
    shadow("org.javassist:javassist:3.29.2-GA")
    shadow("com.logisticscraft:occlusionculling:0.0.5-SNAPSHOT")
    shadow(platform("org.lwjgl:lwjgl-bom:3.3.5"))

    shadow("org.lwjgl:lwjgl")
    shadow("org.lwjgl:lwjgl-glfw")
    shadow("org.lwjgl:lwjgl-openal")
    shadow("org.lwjgl:lwjgl-opengl")

    arrayOf("linux", "windows", "macos", "windows-arm64", "macos-arm64").forEach { platform ->
        shadow("org.lwjgl:lwjgl::natives-$platform")
        shadow("org.lwjgl:lwjgl-glfw::natives-$platform")
        shadow("org.lwjgl:lwjgl-openal::natives-$platform")
        shadow("org.lwjgl:lwjgl-opengl::natives-$platform")
    }

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
    // built with mapping io jank <3
    mappings(files("calamus-yarn-1.8.9+build.565.jar"))
    implementation("org.javassist:javassist:3.29.2-GA")
    implementation("com.logisticscraft:occlusionculling:0.0.5-SNAPSHOT")
    implementation(platform("org.lwjgl:lwjgl-bom:3.3.5"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-openal")
    implementation("org.lwjgl:lwjgl-opengl")

    arrayOf("linux", "windows", "macos", "windows-arm64", "macos-arm64").forEach { platform ->
        runtimeOnly("org.lwjgl:lwjgl::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-glfw::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-openal::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-opengl::natives-$platform")
    }

    modImplementation("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")
    ploceus.dependOsl("0.16.3")
}

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl")
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

    intermediaryUrl = "https://maven.ornithemc.net/releases/net/ornithemc/calamus-intermediary/1.8.9/calamus-intermediary-1.8.9-v2.jar"
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
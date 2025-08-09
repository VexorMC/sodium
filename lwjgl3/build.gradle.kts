plugins {
    id("java-library")
}

group = "dev.vexor"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:3.3.6"))
    implementation("org.jetbrains:annotations:26.0.2")

    compileOnly("org.lwjgl:lwjgl")
    compileOnly("org.lwjgl:lwjgl-glfw")
    compileOnly("org.lwjgl:lwjgl-openal")
    compileOnly("org.lwjgl:lwjgl-opengl")

    arrayOf("linux", "windows", "macos", "windows-arm64", "macos-arm64").forEach { platform ->
        runtimeOnly("org.lwjgl:lwjgl::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-glfw::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-openal::natives-$platform")
        runtimeOnly("org.lwjgl:lwjgl-opengl::natives-$platform")
    }

    compileOnly("net.java.jinput:jinput:2.0.10")
}

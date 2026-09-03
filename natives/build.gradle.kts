plugins {
    `java-library`
    `maven-publish`
}

val modId = project.property("mod_id") as String
val modName = project.property("mod_name") as String
val modAuthor = project.property("mod_author") as String
val javaVersion = project.property("java_version") as String

base {
    archivesName.set("$modId-natives")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
}

repositories {
    mavenCentral()
}

val nativeExtraction by configurations.creating
val nativePlatforms = mapOf(
    "Windows64" to "windows-x86_64",
    "Windows_ARM64" to "windows-aarch64",
    "Linux64" to "linux-x86_64",
    "Linux_ARM64" to "linux-aarch64",
    "MacOSX64" to "macos-x86_64",
    "MacOSX_ARM64" to "macos-aarch64",
)

dependencies {
    implementation("com.github.stephengold:jolt-jni-Windows64:6.0.0")

    nativeExtraction("com.github.stephengold:jolt-jni-Windows64:6.0.0:ReleaseDp")
    nativeExtraction("com.github.stephengold:jolt-jni-Windows_ARM64:6.0.0:ReleaseDp")

    nativeExtraction("com.github.stephengold:jolt-jni-Linux64:6.0.0:ReleaseDp")
    nativeExtraction("com.github.stephengold:jolt-jni-Linux_ARM64:6.0.0:ReleaseDp")

    nativeExtraction("com.github.stephengold:jolt-jni-MacOSX64:6.0.0:ReleaseDp")
    nativeExtraction("com.github.stephengold:jolt-jni-MacOSX_ARM64:6.0.0:ReleaseDp")
}

tasks.processResources {
    nativePlatforms.forEach { (artifactName, platformName) ->
        from(
            zipTree(
                nativeExtraction.files.single {
                    it.name.contains("jolt-jni-$artifactName")
                }
            )
        ) {
            include("**/*.dll", "**/*.so", "**/*.dylib")

            eachFile {
                relativePath = RelativePath(true, "natives", platformName, name)
            }
        }
    }

    dependsOn(compileNatives)
}

// Automatically generate C/C++ JNI headers into src/main/cpp/include when compiling Java classes
tasks.withType<JavaCompile> {
    options.headerOutputDirectory.set(file("src/main/cpp/include"))
}

// -------------------------------------------------------------
// Automated C++ / CMake Compilation
// -------------------------------------------------------------

val cppDir = file("src/main/cpp")
val cmakeBuildDir = layout.buildDirectory.dir("intermediates/cmake").get().asFile
val skipNatives = project.hasProperty("skipNatives")

fun isCMakeAvailable(): Boolean {
    return try {
        val process = ProcessBuilder("cmake", "--version").start()
        process.waitFor() == 0
    } catch (e: Exception) {
        false
    }
}

val hasCMake = isCMakeAvailable()
var configureSucceeded = false

val configureNatives = tasks.register<Exec>("configureNatives") {
    group = "native"
    description = "Configures CMake for native C++ library"
    dependsOn(tasks.named("compileJava"))

    workingDir = cppDir
    isIgnoreExitValue = true

    doFirst {
        cmakeBuildDir.mkdirs()
    }

    commandLine(
        "cmake",
        "-B", cmakeBuildDir.absolutePath,
        "-S", cppDir.absolutePath,
        "-DCMAKE_BUILD_TYPE=Release"
    )

    onlyIf {
        if (skipNatives) {
            logger.lifecycle("[Interactivity-Natives] Skipping CMake configure (-PskipNatives provided)")
            false
        } else if (!hasCMake) {
            logger.warn("[Interactivity-Natives] CMake was not detected on PATH. Skipping native C++ build. Using existing binaries in resources if available.")
            false
        } else {
            true
        }
    }

    doLast {
        val exit = executionResult.orNull?.exitValue ?: -1
        if (exit == 0) {
            configureSucceeded = true
            logger.lifecycle("[Interactivity-Natives] CMake configuration succeeded.")
        } else {
            configureSucceeded = false
            logger.warn("[Interactivity-Natives] CMake configuration failed (exit code $exit). Ensure a C++ compiler (MSVC, Clang, or GCC) is installed. Proceeding without fresh native build.")
        }
    }
}

val compileNatives = tasks.register<Exec>("compileNatives") {
    group = "native"
    description = "Compiles native C++ shared library using CMake"
    dependsOn(configureNatives)

    workingDir = cppDir
    isIgnoreExitValue = true

    commandLine(
        "cmake",
        "--build", cmakeBuildDir.absolutePath,
        "--config", "Release"
    )

    onlyIf {
        configureSucceeded
    }

    doLast {
        val exit = executionResult.orNull?.exitValue ?: -1
        if (exit == 0) {
            logger.lifecycle("[Interactivity-Natives] Native library successfully compiled!")
        } else {
            logger.warn("[Interactivity-Natives] CMake build failed with exit code $exit. Check compiler errors above.")
        }
    }
}

// Ensure natives are compiled before resources are processed into the JAR
tasks.named("processResources") {
    dependsOn(compileNatives)
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to "$modName Natives",
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to archiveVersion.get(),
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor" to modAuthor
            )
        )
    }
}

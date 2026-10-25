plugins {
    `java-library`
    `maven-publish`
    id("interactivity-base")
}

val modId = commonMod.id
val modName = commonMod.name
val modAuthor = commonMod.author
val javaVersion = commonMod.prop("java.version")

val joltJniVersion = commonMod.dep("jolt_jni")

base {
    archivesName.set("$modId-natives")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
}


val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch").lowercase()
val isWindows = osName.contains("win")
val isMac = osName.contains("mac")
val isAarch64 = osArch.contains("aarch64") || osArch.contains("arm64")

val nativeExtraction by configurations.creating
val joltHeaders by configurations.creating
val joltDevLib by configurations.creating

data class NativeTarget(val classifier: String, val platformDir: String, val targetFileName: String)

val joltNativeTargets = listOf(
    NativeTarget("windows-x64-ReleaseDp", "windows-x86_64", "joltjni.dll"),
    NativeTarget("linux-x64-ReleaseDp", "linux-x86_64", "libjoltjni.so"),
    NativeTarget("linux-arm64-ReleaseDp", "linux-aarch64", "libjoltjni.so"),
    NativeTarget("macos-x64-ReleaseDp", "macos-x86_64", "libjoltjni.dylib"),
    NativeTarget("macos-arm64-ReleaseDp", "macos-aarch64", "libjoltjni.dylib")
)

dependencies {
    api("interactorsmc:jolt-jni:$joltJniVersion@jar")

    joltHeaders("interactorsmc:jolt-headers:$joltJniVersion@zip")

    joltNativeTargets.forEach { target ->
        val ext = when {
            target.classifier.startsWith("windows") -> "dll"
            target.classifier.startsWith("linux") -> "so"
            target.classifier.startsWith("macos") -> "dylib"
            else -> error("Unknown platform")
        }
        nativeExtraction("interactorsmc:joltjni:$joltJniVersion:${target.classifier}@$ext")
    }

    joltDevLib("interactorsmc:joltjni:$joltJniVersion:windows-x64-ReleaseDp@lib")
}

// Директории внутри build/ (не засоряют git)
val joltDir = layout.buildDirectory.dir("jolt")
val joltIncludeDir = joltDir.map { it.dir("include") }
val joltLibDir = joltDir.map { it.dir("lib") }

// Распаковка C++ хедеров Jolt
val extractJoltHeaders by tasks.registering(Sync::class) {
    group = "native"
    description = "Extracts Jolt C++ headers for CMake"
    from(joltHeaders.elements.map { files -> files.map { zipTree(it.asFile) } })
    into(joltIncludeDir)
}

val prepareJoltLib by tasks.registering(Copy::class) {
    group = "native"
    description = "Prepares host Jolt native library for CMake linking"
    into(joltLibDir)

    val currentClassifier = when {
        isWindows -> "windows-x64-ReleaseDp"
        isMac -> if (isAarch64) "macos-arm64-ReleaseDp" else "macos-x64-ReleaseDp"
        else -> if (isAarch64) "linux-arm64-ReleaseDp" else "linux-x64-ReleaseDp"
    }

    if (isWindows) {
        from(joltDevLib) {
            include("*$currentClassifier*.lib")
            rename { "joltjni.lib" }
        }
        from(nativeExtraction) {
            include("*$currentClassifier*.dll")
            rename { "joltjni.dll" }
        }
    } else if (isMac) {
        from(nativeExtraction) {
            include("*$currentClassifier*.dylib")
            rename { "libjoltjni.dylib" }
        }
    } else {
        from(nativeExtraction) {
            include("*$currentClassifier*.so")
            rename { "libjoltjni.so" }
        }
    }
}

// Automatically generate C/C++ JNI headers into src/main/cpp/include when compiling Java classes
tasks.withType<JavaCompile> {
    options.headerOutputDirectory.set(file("src/main/cpp/include"))
}

val cppDir = file("src/main/cpp")
val cmakeBuildDir = layout.buildDirectory.dir("intermediates/cmake").get().asFile
val skipNatives = project.hasProperty("skipNatives")

fun isCMakeAvailable(): Boolean {
    return try {
        // todo: make compatible with configuration cache
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
    dependsOn(tasks.named("compileJava"), extractJoltHeaders, prepareJoltLib)

    workingDir = cppDir
    isIgnoreExitValue = true

    doFirst {
        cmakeBuildDir.mkdirs()
    }

    commandLine(
        "cmake",
        "-B", cmakeBuildDir.absolutePath,
        "-S", cppDir.absolutePath,
        "-DCMAKE_BUILD_TYPE=Release",
        "-DJOLT_INCLUDE_DIR=${joltIncludeDir.get().asFile.absolutePath}",
        "-DJOLT_LIB_DIR=${joltLibDir.get().asFile.absolutePath}"
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

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    joltNativeTargets.forEach { target ->
        from(nativeExtraction) {
            include("*${target.classifier}*")
            into("natives/${target.platformDir}")
            rename { target.targetFileName }
        }
    }

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
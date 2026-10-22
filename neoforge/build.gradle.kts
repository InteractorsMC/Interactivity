plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

val neoforgeVersion = project.property("neoforge_version") as String
val parchmentMinecraft = project.property("parchment_minecraft") as String
val parchmentVersion = project.property("parchment_version") as String
val modId = project.property("mod_id") as String

neoForge {
    version.set(neoforgeVersion)
    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion.set(parchmentMinecraft)
        mappingsVersion.set(parchmentVersion)
    }
    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            ideName.set("NeoForge ${name.replaceFirstChar { it.uppercase() }} (${project.path})") // Unify the run config names with fabric
        }
        create("client") {
            client()
        }
        create("server") {
            server()
        }
    }
    mods {
        create(modId) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    implementation(project(":natives"))
    jarJar(project(":natives"))
    additionalRuntimeClasspath(project(":natives"))
    // todo: add jolt-jni; add build-extensions for dependencies

    // Dependencies
    val midnightlib = "eu.midnightdust:midnightlib:${project.property("midnightlib_version")}-neoforge"
    implementation(midnightlib)
    jarJar(midnightlib)
}


sourceSets["main"].resources.srcDir("src/generated/resources")

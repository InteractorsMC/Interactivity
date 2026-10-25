plugins {
    id("multiloader-loader")
    id("fabric-loom")
}

// todo: use similar to Friends&Foes build-extensions
val enableOptimizationMods = (project.property("enable_optimization_mods") as String).toBoolean()
val minecraftVersion = project.property("minecraft_version") as String
val parchmentMinecraft = project.property("parchment_minecraft") as String
val parchmentVersion = project.property("parchment_version") as String
val fabricLoaderVersion = project.property("fabric_loader_version") as String
val fabricVersion = project.property("fabric_version") as String
val modMenuVersion = project.property("modmenu_version") as String
val midnightLibVersion = project.property("midnightlib_version") as String
val sodiumVersion = project.property("sodium_version") as String
val irisVersion = project.property("iris_version") as String
val joltJniVersion = project.findProperty("jolt_jni_version") as? String
val modId = project.property("mod_id") as String

dependencies {
    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$parchmentMinecraft:$parchmentVersion@zip")
    })
    "modImplementation"("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    "modImplementation"("net.fabricmc.fabric-api:fabric-api:$fabricVersion")

    include(project(":natives"))
    include("interactorsmc:jolt-jni:$joltJniVersion@jar")

    // Dependencies
    val midnightlib = "eu.midnightdust:midnightlib:$midnightLibVersion-fabric"
    "modImplementation"(midnightlib)
    include(midnightlib)

    "modImplementation"("com.terraformersmc:modmenu:$modMenuVersion")

    if (enableOptimizationMods) {
        "modLocalRuntime"("maven.modrinth:sodium:$sodiumVersion-fabric")

        if (irisVersion.isNotEmpty()) {
            "modLocalRuntime"("maven.modrinth:iris:$irisVersion-fabric")
            localRuntime("org.anarres:jcpp:1.4.14")
            localRuntime("org.antlr:antlr4-runtime:4.13.1")
            localRuntime("io.github.douira:glsl-transformer:3.0.0-pre3")
        }
    }
}

loom {
    val aw = project(":common").file("src/main/resources/$modId.accesswidener")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }
    mixin {
        defaultRefmapName.set("$modId.refmap.json")
    }
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("runs/client")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("runs/server")
        }
    }
}

plugins {
    id("multiloader-loader")
    id("fabric-loom")
}

dependencies {
    "minecraft"("com.mojang:minecraft:${commonMod.dep("minecraft")}")
    "mappings"(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${commonMod.dep("parchment.mc")}:${commonMod.dep("parchment.version")}@zip")
    })
    "modImplementation"("net.fabricmc:fabric-loader:${commonMod.dep("fabric_loader")}")
    "modImplementation"("net.fabricmc.fabric-api:fabric-api:${commonMod.dep("fabric_api")}")

    // Dependencies
    val midnightlib = "eu.midnightdust:midnightlib:${commonMod.dep("midnightlib")}-fabric"
    "modImplementation"(midnightlib)
    include(midnightlib)

    "modImplementation"("com.terraformersmc:modmenu:${commonMod.dep("mod_menu")}")

    if (commonMod.prop("enable_optimization_mods").toBoolean()) {
        "modLocalRuntime"(commonMod.modrinth("sodium", "${commonMod.dep("sodium")}-fabric"))

        commonMod.depOrNull("iris")?.let { irisVersion ->
            "modLocalRuntime"(commonMod.modrinth("iris", "$irisVersion-fabric"))
            localRuntime("org.anarres:jcpp:1.4.14")
            localRuntime("org.antlr:antlr4-runtime:4.13.1")
            localRuntime("io.github.douira:glsl-transformer:3.0.0-pre3")
        }
    }
}

loom {
    val aw = project(":common").file("src/main/resources/${commonMod.id}.accesswidener")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }
    mixin {
        defaultRefmapName.set("${commonMod.id}.refmap.json")
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

tasks.shadowJar {
    archiveClassifier.set("dev-shadow")
}

// Remap the shadowed jar instead of the plain one
tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
}

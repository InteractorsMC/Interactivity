plugins {
    id("multiloader-loader")
    id("fabric-loom")
}

val minecraftVersion = project.property("minecraft_version") as String
val parchmentMinecraft = project.property("parchment_minecraft") as String
val parchmentVersion = project.property("parchment_version") as String
val fabricLoaderVersion = project.property("fabric_loader_version") as String
val fabricVersion = project.property("fabric_version") as String
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
    val midnightlib = "eu.midnightdust:midnightlib:${project.property("midnightlib_version")}-fabric"
    "modImplementation"(midnightlib)
    include(midnightlib)

    "modImplementation"("com.terraformersmc:modmenu:${project.property("modmenu_version")}")
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
            setConfigName("Fabric Client")
            ideConfigGenerated(true)
            runDir("runs/client")
        }
        named("server") {
            server()
            setConfigName("Fabric Server")
            ideConfigGenerated(true)
            runDir("runs/server")
        }
    }
}

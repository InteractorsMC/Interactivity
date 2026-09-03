plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

val neoforgeVersion = project.property("neoforge_version") as String
val parchmentMinecraft = project.property("parchment_minecraft") as String
val parchmentVersion = project.property("parchment_version") as String
val modId = project.property("mod_id") as String

dependencies {
    jarJar(project(":natives"))
}

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
        create("data") {
            data()

            // DataGen can be run by - "./gradlew :neoforge:runData" in Terminal.
            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll(
                listOf(
                    "--mod", modId,
                    "--all",
                    "--output", file("src/generated/resources/").absolutePath,
                    "--existing", file("src/main/resources/").absolutePath
                )
            )
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

sourceSets["main"].resources.srcDir("src/generated/resources")

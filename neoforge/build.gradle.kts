plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

neoForge {
    version = commonMod.dep("neoforge")

    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion.set(commonMod.dep("parchment.mc"))
        mappingsVersion.set(commonMod.dep("parchment.version"))
    }
    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", commonMod.id)
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
        create(commonMod.id) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    implementation(project(":natives"))
    "additionalRuntimeClasspath"(project(":natives"))

    // Dependencies
    val midnightlib = "eu.midnightdust:midnightlib:${commonMod.dep("midnightlib")}-neoforge"
    implementation(midnightlib)
    jarJar(midnightlib)

    // todo: optimization mods
}


sourceSets["main"].resources.srcDir("src/generated/resources")

tasks.shadowJar {
    archiveClassifier.set("shadow-intermediate")
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    from(zipTree(tasks.shadowJar.flatMap { it.archiveFile })) {
        exclude("META-INF/MANIFEST.MF")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion = commonMod.dep("neoform")
    // Automatically enable AccessTransformers if the file exists
    val at = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = commonMod.dep("parchment.mc")
        mappingsVersion = commonMod.dep("parchment.version")
    }

    runs {
        create("data") { // todo: finish datagen by feeding it's results to artifacts, or remove if not required for this project (probably gonna happen)
            data()

            // DataGen can be run by - "./gradlew :neoforge:runData" in Terminal.
            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll(
                listOf(
                    "--mod", commonMod.id,
                    "--all",
                    "--output", file("src/generated/resources/").absolutePath,
                    "--existing", file("src/main/resources/").absolutePath
                )
            )
        }

    }
}

dependencies {
    api(project(":natives"))
    compileOnly("org.spongepowered:mixin:0.8.5")
    // fabric and neoforge both bundle mixinextras, so it is safe to use it in common
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")

    // Dependencies
    val midnightlib = "eu.midnightdust:midnightlib:${commonMod.dep("midnightlib")}-fabric"
    compileOnly(midnightlib)
}

val commonJava by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

val commonResources by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts {
    add("commonJava", sourceSets["main"].java.sourceDirectories.singleFile)
    add("commonResources", sourceSets["main"].resources.sourceDirectories.singleFile)
}

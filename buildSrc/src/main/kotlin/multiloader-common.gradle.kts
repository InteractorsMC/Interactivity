plugins {
    `java-library`
    `maven-publish`
    id("interactivity-base")
}

val modId = commonMod.id
val modName = commonMod.name
val modAuthor = commonMod.author
val minecraftVersion = commonMod.dep("minecraft")
val javaVersion = commonMod.prop("java.version")

base {
    archivesName.set("$modId-${project.name}-$minecraftVersion")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    // https://docs.gradle.org/current/userguide/declaring_repositories.html#declaring_content_exclusively_found_in_one_repository
    exclusiveContent {
        forRepository {
            maven {
                name = "Sponge"
                url = uri("https://repo.spongepowered.org/repository/maven-public")
            }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }
    exclusiveContent {
        forRepositories(
            maven {
                name = "ParchmentMC"
                url = uri("https://maven.parchmentmc.org/")
            },
            maven {
                name = "NeoForge"
                url = uri("https://maven.neoforged.net/releases")
            }
        )
        filter { includeGroup("org.parchmentmc.data") }
    }
    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com")
    }
    maven {
        name = "MidnightDust"
        url = uri("https://maven.midnightdust.eu/releases")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
    }
}

// Declare capabilities on the outgoing configurations.
// Read more about capabilities here: https://docs.gradle.org/current/userguide/component_capabilities.html#sec:declaring-additional-capabilities-for-a-local-component
listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach { variant ->
    configurations.named(variant) {
        outgoing {
            capability("$group:${base.archivesName.get()}:$version")
            capability("$group:$modId-${project.name}-$minecraftVersion:$version")
            capability("$group:$modId:$version")
        }
    }
    publishing.publications.withType<MavenPublication>().configureEach {
        suppressPomMetadataWarningsFor(variant)
    }
}

tasks.named<Jar>("sourcesJar") {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_$modName" }
    }
}

tasks.named<Jar>("jar") {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_$modName" }
    }

    manifest {
        attributes(
            mapOf(
                "Specification-Title" to modName,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to archiveVersion.get(),
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor" to modAuthor,
                "Built-On-Minecraft" to minecraftVersion
            )
        )
    }
}

tasks.named<ProcessResources>("processResources") {
    val expandProps = mapOf(
        "version" to project.version,
        "group" to project.group, // Else we target the task's group.
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to commonMod.dep("minecraft.range"),
        "fabric_version" to commonMod.dep("fabric_api"),
        "fabric_loader_version" to commonMod.dep("fabric_loader"),
        "mod_name" to modName,
        "mod_author" to modAuthor,
        "mod_id" to modId,
        "license" to commonMod.license,
        "description" to commonMod.description,
        "neoforge_version" to commonMod.dep("neoforge"),
        "neoforge_version_range" to commonMod.dep("neoforge.range"),
        "credits" to commonMod.credits,
        "java_version" to javaVersion
    )

    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
        expand(expandProps)
    }
    inputs.properties(expandProps)
}

tasks.withType<Javadoc> {
    options {
        this as StandardJavadocDocletOptions
        tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }
}


publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
    repositories {
        val localMavenUrl = System.getenv("local_maven_url")
        if (!localMavenUrl.isNullOrEmpty()) {
            maven {
                url = uri(localMavenUrl)
            }
        }
    }
}

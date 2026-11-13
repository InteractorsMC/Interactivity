import io.freefair.gradle.plugins.lombok.tasks.Delombok

plugins {
    id("io.freefair.lombok")
}

group = commonMod.group
version = commonMod.version

repositories {
    mavenCentral()

    ivy {
        name = "InteractorsJoltJni"
        url = uri("https://github.com/InteractorsMC/jolt-jni/releases/download/${commonMod.dep("jolt_jni")}/")
        patternLayout {
            artifact("[artifact]-[revision](-[classifier]).[ext]")
        }
        metadataSources {
            artifact()
        }
        content {
            includeGroup("interactorsmc")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

// Registered only when credentials are present (CI), so local `publish` runs don't hit the remote repo
pluginManager.withPlugin("maven-publish") {
    extensions.configure<PublishingExtension> {
        repositories {
            val repoUser = System.getenv("REPO_USER")
            val repoPassword = System.getenv("REPO_PASSWORD")
            if (!repoUser.isNullOrEmpty() && !repoPassword.isNullOrEmpty()) {
                maven {
                    name = "Interactors"
                    val isSnapshot = project.version.toString().contains("SNAPSHOT")
                    url = uri(commonMod.prop(if (isSnapshot) "maven.snapshots" else "maven.releases"))
                    credentials {
                        username = repoUser
                        password = repoPassword
                    }
                }
            }
        }
    }
}

lombok {
    version = "1.18.30"
}

tasks.withType<Jar>().matching { it.name == "sourcesJar" }.configureEach {
    val javaDirs = project.extensions.getByType<SourceSetContainer>()["main"].java.srcDirs
    exclude { element -> javaDirs.any { element.file.startsWith(it) } }
    from(tasks.named<Delombok>("delombok"))
}
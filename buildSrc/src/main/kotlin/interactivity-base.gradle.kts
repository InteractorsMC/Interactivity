val joltJniVersion = project.findProperty("jolt_jni_version") as? String

repositories {
    mavenCentral()

    ivy {
        name = "InteractorsJoltJni"
        url = uri("https://github.com/InteractorsMC/jolt-jni/releases/download/$joltJniVersion/")
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

dependencies {
    add("compileOnly", "org.projectlombok:lombok:1.18.30")
    add("annotationProcessor", "org.projectlombok:lombok:1.18.30")
    add("testCompileOnly", "org.projectlombok:lombok:1.18.30")
    add("testAnnotationProcessor", "org.projectlombok:lombok:1.18.30")
}
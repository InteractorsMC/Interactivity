plugins {
    id("multiloader-common")
    id("com.gradleup.shadow")
}

val shade = configurations.create("shade") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

val commonJava = configurations.create("commonJava") {
    isCanBeResolved = true
}
val commonResources = configurations.create("commonResources") {
    isCanBeResolved = true
}

val group = project.group.toString()

dependencies {
    val commonProject = project(":common")
    commonProject.capabilities {
        requireCapability("$group:${commonMod.id}")
    }
    compileOnly(commonProject)

    implementation(project(":natives"))
    add("shade", project(mapOf("path" to ":natives", "configuration" to "shadowRuntimeElements")))

    add("commonJava", project(mapOf("path" to ":common", "configuration" to "commonJava")))
    add("commonResources", project(mapOf("path" to ":common", "configuration" to "commonResources")))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(commonResources)
    from(commonResources)
}

tasks.named<Javadoc>("javadoc") {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(commonJava)
    from(commonJava)
    dependsOn(commonResources)
    from(commonResources)
}

tasks.shadowJar {
    configurations = listOf(shade)
    mergeServiceFiles()
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.6")
    implementation("io.freefair.lombok:io.freefair.lombok.gradle.plugin:9.8.3")
}

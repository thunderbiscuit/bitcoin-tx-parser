import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
}

group = "me.thunderbiscuit"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk15to18:1.71")
    implementation("com.google.guava:guava:31.1-jre")

    // Ktor
    implementation("io.ktor:ktor-client-core:2.0.1")
    implementation("io.ktor:ktor-client-cio:2.0.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
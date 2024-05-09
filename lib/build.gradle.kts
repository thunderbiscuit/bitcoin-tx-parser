plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.gradle.java-library")
    id("org.gradle.maven-publish")
}

group = "me.tb"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest("1.9.23")
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf(
            "-opt-in=kotlin.ExperimentalUnsignedTypes",
            "-opt-in=kotlin.ExperimentalStdlibApi",
        )
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = groupId
            artifactId = "bitcoin-tx-parser"
            version = version

            from(components["java"])
        }
    }
}

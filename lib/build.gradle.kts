plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.jvm)

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

group = "me.tb"
version = "0.1.0-SNAPSHOT"

dependencies {
    // // This dependency is exported to consumers, that is to say found on their compile classpath.
    // api(libs.commons.math3)
    //
    // // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    // implementation(libs.guava)

    implementation("org.bouncycastle:bcprov-jdk15to18:1.71")
    implementation("com.google.guava:guava:31.1-jre")

    // Ktor
    implementation("io.ktor:ktor-client-core:2.0.1")
    implementation("io.ktor:ktor-client-cio:2.0.1")

    testImplementation(kotlin("test"))
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest("1.9.20")
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

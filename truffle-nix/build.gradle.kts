/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("buildlogic.java-application-conventions")
    id("com.diffplug.spotless") version "7.0.0.BETA4"
}

dependencies {
    implementation("io.github.tree-sitter:jtreesitter:0.24.0")
    implementation(project(":tree-sitter-nix"))

    implementation("org.graalvm.truffle:truffle-api:24.1.1")
    implementation("org.graalvm.truffle:truffle-runtime:24.1.1")
    annotationProcessor("org.graalvm.truffle:truffle-dsl-processor:24.1.1")
}

application {
    // Define the main class for the application.
    mainClass = "website.lihan.trufflenix.Main"
}

tasks.test {
    jvmArgs(
        "-ea",
        "--add-exports",
        "org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED",
        "--add-exports",
        "org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED",
        "--add-exports",
        "org.graalvm.truffle/com.oracle.truffle.api.staticobject=ALL-UNNAMED",
    )

    environment("LD_LIBRARY_PATH", "/home/lh/src/truffle-nix/tree-sitter-nix/src/main/resources")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    format("misc") {
        target("*.gradle", ".gitattributes", ".gitignore")

        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }

    kotlinGradle {
        ktlint()
    }

    java {
        importOrder()

        googleJavaFormat()

        formatAnnotations()
    }
}

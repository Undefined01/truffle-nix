/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("buildlogic.java-application-conventions")
    id("com.diffplug.spotless") version "7.0.0.BETA4"
    id("me.champeau.jmh") version "0.7.2"
}

dependencies {
    implementation("io.github.tree-sitter:jtreesitter:0.24.0")
    implementation(project(":tree-sitter-nix"))

    implementation("org.graalvm.truffle:truffle-api:24.1.1")
    implementation("org.graalvm.truffle:truffle-runtime:24.1.1")
    annotationProcessor("org.graalvm.truffle:truffle-dsl-processor:24.1.1")

    implementation("org.graalvm.truffle:truffle-sl:24.1.1")
    jmh("org.graalvm.truffle:truffle-sl:24.1.1")
    jmh("org.graalvm.js:js:24.1.1")
}

application {
    // Define the main class for the application.
    mainClass = "website.lihan.trufflenix.Main"
}

tasks.test {
    jvmArgs(
        "-Dgraalvm.locatorDisabled=true",
        "--enable-native-access=ALL-UNNAMED",
        "-Djava.library.path=${project(":tree-sitter-nix").projectDir}/src/main/resources",
    )

    filter {
        // includeTestsMatching("*quicksort*")
    }

    environment("LD_LIBRARY_PATH", "${project(":tree-sitter-nix").projectDir}/src/main/resources")
}

tasks.jmh {
    // includes = listOf("Fibonacci.nix")
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

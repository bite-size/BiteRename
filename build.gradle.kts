import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

plugins {
    kotlin("jvm") version "2.4.0"
    id("com.gradleup.shadow") version "9.4.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "gg.bitesize"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://jitpack.io") {
        content { includeGroup("com.github.MilkBowl") }
    }
}

dependencies {
    // Spigot API is the common surface of Spigot, Paper, and Purpur
    compileOnly("org.spigotmc:spigot-api:26.3-R0.1-SNAPSHOT")

    // Downloaded at runtime via plugin.yml `libraries`
    compileOnly(kotlin("stdlib"))

    // Optional integrations, provided by the server when installed
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.12.3") { isTransitive = false }

    implementation("net.kyori:adventure-text-minimessage:5.2.0")
    implementation("net.kyori:adventure-text-serializer-legacy:5.2.0")
    implementation("net.kyori:adventure-text-serializer-plain:5.2.0")
    implementation("org.bstats:bstats-bukkit:3.2.1")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    jar {
        archiveClassifier.set("plain")
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("net.kyori", "gg.bitesize.rename.libs.kyori")
        relocate("org.bstats", "gg.bitesize.rename.libs.bstats")
        mergeServiceFiles()
        exclude("META-INF/maven/**", "META-INF/versions/*/module-info.class", "module-info.class", "org/jspecify/**")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        val props = mapOf(
            "version" to project.version,
            "kotlinVersion" to project.getKotlinPluginVersion(),
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    runServer {
        minecraftVersion("26.3")
    }
}

kotlin {
    jvmToolchain(25)
}

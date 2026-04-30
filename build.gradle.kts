plugins {
    id("fabric-loom")
    java
    kotlin("jvm") version "2.1.0"
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.gegy.dev")
    maven("https://maven.shedaniel.me")
    maven("https://maven.isxander.dev/releases") {
        content { includeGroup("dev.isxander") }
    }
    maven("https://maven.quiltmc.org/repository/release") {
        content { includeGroup("org.quiltmc.parsers") }
    }
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.ladysnake.org/releases")
    maven("https://jitpack.io") {
        content { excludeGroup("dev.isxander") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("com.terraformersmc:modmenu:${property("mod_menu_version")}")
    modImplementation("dev.isxander:yet-another-config-lib:${property("yacl_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${property("rei_version")}")

    setOf(
        "fabric-api-base",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",
        "fabric-rendering-v1"
    ).forEach {
        modImplementation(fabricApi.module(it, property("fabric_version") as String))
    }

    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:${property("rei_version")}")
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.1.2")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }

    compileJava {
        options.release = 21
    }
}

loom {
    mixin {
        defaultRefmapName = "void.refmap.json"
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}
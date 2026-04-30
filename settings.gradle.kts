pluginManagement {
	repositories {
		mavenCentral()
		maven("https://maven.fabricmc.net/") {
			name = "Fabric"
		}
		gradlePluginPortal()
	}

    plugins {
        id("fabric-loom") version "1.13-SNAPSHOT"
        java
        kotlin("jvm") version "2.1.0"
    }
}
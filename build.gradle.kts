plugins {
    id("fabric-loom") version "0.11-SNAPSHOT"
    kotlin("jvm") version "1.6.10"
}

object Constants {
    const val modVersion = "3.0.0-alpha.3"
    const val modName = "manhunt3"
    const val mavenGroup = "org.featurehouse.mcmod"

    const val mcVersion = "1.18.1"
    const val yarnMappings = "1.18.1+build.20"
    const val loaderVersion = "0.12.12"

    const val fabricVersion = "0.46.4+1.18"
    const val fabricKotlinVersion = "1.7.1+kotlin.1.6.10"
}
val env: Map<String, String> = System.getenv()

base.archivesName.set(Constants.modName)

group = Constants.mavenGroup
version = Constants.modVersion

repositories {
    if (!env.containsKey("DONT_USE_ALIYUN_MIRROR")) {
        maven(url = "https://maven.aliyun.com/repository/public") {
            name = "Aliyun Mirror"
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Constants.mcVersion}")
    mappings("net.fabricmc:yarn:${Constants.yarnMappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${Constants.loaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Constants.fabricVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${Constants.fabricKotlinVersion}")
}

tasks {
    withType<AbstractCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<JavaCompile> { options.encoding = "UTF-8" }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    jar {
        from("license.txt") {
            rename { "LICENSE_${Constants.modName}" }
        }
    }

    processResources {
        inputs.property("version", Constants.modVersion)
        filesMatching("fabric.mod.json") {
            expand("version" to Constants.modVersion)
        }
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

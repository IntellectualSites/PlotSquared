import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("org.spongepowered.gradle.plugin") version "2.0.0"
}

dependencies {
    api(projects.plotSquaredCore)

    compileOnly(libs.worldeditSponge)
}

sponge {
    apiVersion("8.0.0")
    license("All Rights Reserved")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("sponge") {
        displayName("PlotSquared")
        entrypoint("com.plotsquared.sponge.SpongePlatform")
        description("Easy, yet powerful Plot World generation and management.")
        links {
            // homepage("https://spongepowered.org")
            // source("https://spongepowered.org/source")
            // issues("https://spongepowered.org/issues")
        }
        contributor("Citymonstret") {
            description("Author")
        }
        contributor("Empire92") {
            description("Author")
        }
        contributor("MattBDev") {
            description("Author")
        }
        contributor("dordsor21") {
            description("Author")
        }
        contributor("NotMyFault") {
            description("Author")
        }
        contributor("SirYwell") {
            description("Author")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
//        dependency("${DEPEDENCY}") {
//            loadOrder(PluginDependency.LoadOrder.AFTER)
//            optional(false)
//        }
    }
}

val javaTarget = 16
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
    if (JavaVersion.current() < JavaVersion.toVersion(javaTarget)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

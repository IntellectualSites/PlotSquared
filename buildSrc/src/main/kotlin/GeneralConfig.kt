import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

fun Project.applyGeneralConfiguration() {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven {
            name = "Sonatype OSS"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            name = "Jitpack"
            url = uri("https://jitpack.io")
        }
        maven {
            name = "IntellectualSites Repository"
            url = uri("https://mvn.intellectualsites.com/content/repositories/snapshots")
        }
        maven {
            name = "EngineHub Repository"
            url = uri("https://maven.enginehub.org/repo/")
        }
    }

    dependencies {
        "implementation"("com.google.guava:guava:${Versions.GUAVA}")
        "testImplementation"("junit:junit:${Versions.JUNIT}")
        "compileOnly"("javax.annotation:javax.annotation-api:1.3.2")
        "compileOnly"("org.json:json:20200518")
        "implementation"("com.sk89q.worldedit:worldedit-core:7.2.0-SNAPSHOT") {
            exclude(group = "bukkit-classloader-check")
            exclude(group = "mockito-core")
            exclude(group = "dummypermscompat")
        }
    }

    configurations.all {
        resolutionStrategy {
            force("com.google.guava:guava:${Versions.GUAVA}")
            force("com.google.code.findbugs:jsr305:3.0.2")
        }
    }

    tasks.named<ShadowJar>("shadowJar") {
        dependencies {
            include(dependency("org.json:json:20200518"))
            include(dependency("net.kyori:text-api:3.0.2"))
            include(dependency("javax.inject:javax.inject:1"))
            include(dependency("aopalliance:aopalliance:1.0"))
        }
        relocate("io.papermc.lib", "com.plotsquared.bukkit.paperlib")
        relocate("org.json", "com.plotsquared.json") {
            exclude("org/json/simple/**")
        }
        archiveFileName.set("${project.name}-${project.version}.jar")
        destinationDirectory.set(file("../target"))
    }

    apply(plugin = "net.minecrell.licenser")
    configure<LicenseExtension> {
        header = rootProject.file("HEADER.txt")
        include("**/*.java")
    }
}

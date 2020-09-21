import org.ajoberstar.grgit.Grgit
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyTasksConfiguration()

buildscript {
    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:5.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
    }
    configurations.all {
        resolutionStrategy {
            force("org.ow2.asm:asm:8.0.1")
        }
    }
}

plugins {
    kotlin("multiplatform") version "1.3.72"
    id("maven-publish")
}

group = "com.plotsquared"

apply(plugin = "org.ajoberstar.grgit")
ext {
    val git: Grgit = Grgit.open {
        dir = File("$rootDir/.git")
    }
}

var ver by extra("6.0.0")
var versuffix by extra("-SNAPSHOT")
ext {
    if (project.hasProperty("versionsuffix")) {
        //    versuffix = "-$versionsuffix"
    }
}
version = ver + versuffix

allprojects {

    gradle.projectsEvaluated {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "1000"))
        }
    }
}

subprojects {
    applyTasksConfiguration()

    dependencies {
        "compileOnly"("org.json:json:20200518")
        "implementation"("com.sk89q.worldedit:worldedit-core:7.2.0-SNAPSHOT") {
            exclude(group = "bukkit-classloader-check")
            exclude(group = "mockito-core")
            exclude(group = "dummypermscompat")
        }
        "implementation"("com.google.guava:guava:${Versions.GUAVA}")
        "testImplementation"("junit:junit:${Versions.JUNIT}")
        "compileOnly"("javax.annotation:javax.annotation-api:1.3.2")
    }

    configurations.all {
        resolutionStrategy {
            force("junit:junit:${Versions.JUNIT}")
            force("com.google.guava:guava:${Versions.GUAVA}")
            force("com.google.code.findbugs:jsr305:3.0.2")
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://maven.enginehub.org/repo/") }
        maven { url = uri("https://repo.maven.apache.org/maven2") }
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

    version = rootProject.version
}
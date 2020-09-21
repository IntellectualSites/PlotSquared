import org.ajoberstar.grgit.Grgit
import net.minecrell.gradle.licenser.LicenseExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript {
    repositories {
        mavenCentral()
        maven {
            name = "Sonatype OSS"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        jcenter()
    }
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
    id("org.ajoberstar.grgit") version "4.0.2"
    id("net.minecrell.licenser") version "0.4.1"
}

group = "com.plotsquared"

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
    apply(plugin = "net.minecrell.licenser")

    gradle.projectsEvaluated {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "1000"))
        }
    }
    configure<LicenseExtension> {
        header = rootProject.file("HEADER")
        include("**/*.java")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "eclipse")
    apply(plugin = "idea")
    apply(plugin = "com.github.johnrengelman.shadow")

    dependencies {
        "compileOnly"("org.json:json:20200518")
        "implementation"("com.sk89q.worldedit:worldedit-core:7.2.0-SNAPSHOT") {
            exclude(group = "bukkit-classloader-check")
            exclude(group = "mockito-core")
            exclude(group = "dummypermscompat")
        }
        "implementation"("com.google.guava:guava:21.0") {
            because("Minecraft uses Guava 21 as of 1.13")
        }
        "testImplementation"("junit:junit:4.13")
        "compileOnly"("javax.annotation:javax.annotation-api:1.3.2")
    }

    configurations.all {
        resolutionStrategy {
            force("junit:junit:4.12")
            force("com.google.guava:guava:21.0")
            force("com.google.code.findbugs:jsr305:3.0.2")
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://maven.enginehub.org/repo/") }
        maven { url = uri("https://repo.maven.apache.org/maven2") }
        maven { url = uri("https://jitpack.io") }
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

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
        )
    }
}

tasks.register<Jar>("javadocJar") {
    dependsOn("javadoc")
    archiveClassifier.set("javadoc")
    from(tasks.getByName<Javadoc>("javadoc").destinationDir)
}

tasks.register<Jar>("sourcesJar") {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

artifacts {
    add("archives", tasks.named("jar"))
    add("archives", tasks.named("javadocJar"))
    add("archives", tasks.named("sourcesJar"))
}
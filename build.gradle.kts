import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.minecrell.gradle.licenser.LicenseExtension
import net.minecrell.gradle.licenser.Licenser

plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("net.minecrell.licenser") version "0.4.1"

    eclipse
    idea
}

var ver by extra("6.0.0")
var versuffix by extra("-SNAPSHOT")
val versionsuffix: String? by project
if (versionsuffix != null) {
    versuffix = "-$versionsuffix"
}
version = ver + versuffix

allprojects {
    group = "com.plotsquared"
    version = rootProject.version

    repositories {
        mavenCentral()
        jcenter()

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
}

subprojects {
    apply {
        plugin<JavaPlugin>()
        plugin<JavaLibraryPlugin>()
        plugin<MavenPublishPlugin>()
        plugin<ShadowPlugin>()
        plugin<Licenser>()

        plugin<EclipsePlugin>()
        plugin<IdeaPlugin>()
    }

    dependencies {
        api("com.sk89q.worldedit:worldedit-core:7.2.0-SNAPSHOT") {
            exclude(group = "bukkit-classloader-check")
            exclude(group = "mockito-core")
            exclude(group = "dummypermscompat")
        }
    }

    tasks {
        // This is to create the target dir under the root project with all jars.
        val assembleTargetDir = create<Copy>("assembleTargetDirectory") {
            destinationDir = rootDir.resolve("target")
            into(destinationDir)
            from(withType<Jar>())
        }
        named("build") {
            dependsOn(assembleTargetDir)
        }
    }
}

allprojects {
    dependencies {
        // Tests
        testImplementation("junit:junit:4.13")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = sourceCompatibility
    }

    configure<LicenseExtension> {
        header = rootProject.file("HEADER.txt")
        include("**/*.java")
        newLine = false
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                // This includes not only the original jar (i.e. not shadowJar),
                // but also sources & javadocs due to the above java block.
                from(components["java"])

                pom {
                    licenses {
                        license {
                            name.set("GNU General Public License, Version 3.0")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("Saulitired")
                            name.set("Alexander Söderberg")
                        }

                        // TODO: Add developers
                        // I don't know the rest of your names, so this is up to
                        // you to fill in.
                    }

                    scm {
                        url.set("https://github.com/IntellectualSites/PlotSquared")
                        connection.set("scm:https://IntellectualSites@github.com/IntellectualSites/PlotSquared.git")
                        developerConnection.set("scm:git://github.com/IntellectualSites/PlotSquared.git")
                    }
                }
            }
        }

        repositories {
            mavenLocal() // Install to own local repository

            // Accept String? to not err if they're not present.
            // Check that they both exist before adding the repo, such that
            // `credentials` doesn't err if one is null.
            // It's not pretty, but this way it can compile.
            val nexusUsername: String? by project
            val nexusPassword: String? by project
            if (nexusUsername != null && nexusPassword != null) {
                maven {
                    val repositoryUrl = "https://mvn.intellectualsites.com/content/repositories/releases/"
                    val snapshotRepositoryUrl = "https://mvn.intellectualsites.com/content/repositories/snapshots/"
                    url = uri(
                        if (version.toString().endsWith("-SNAPSHOT")) snapshotRepositoryUrl
                        else repositoryUrl
                    )

                    credentials {
                        username = nexusUsername
                        password = nexusPassword
                    }
                }
            } else {
                logger.warn("No nexus repository is added; nexusUsername or nexusPassword is null.")
            }
        }
    }

    val javadocDir = rootDir.resolve("docs").resolve("javadoc").resolve(project.name)
    tasks {
        named<Delete>("clean") {
            doFirst {
                delete(rootDir.resolve("target"))
                delete(javadocDir)
            }
        }

        compileJava {
            options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "1000"))
            options.compilerArgs.add("-Xlint:all")
            for (disabledLint in arrayOf("processing", "path", "fallthrough", "serial"))
                options.compilerArgs.add("-Xlint:$disabledLint")
            options.isDeprecation = true
            options.encoding = "UTF-8"
        }

        javadoc {
            val opt = options as StandardJavadocDocletOptions
            opt.addStringOption("Xdoclint:none", "-quiet")
            opt.tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
            opt.destinationDirectory = javadocDir
        }

        shadowJar {
            this.archiveClassifier.set(null as String?)
            this.archiveFileName.set("${project.name}-${project.version}.${this.archiveExtension.getOrElse("jar")}")
            this.destinationDirectory.set(rootProject.tasks.shadowJar.get().destinationDirectory.get())
        }

        named("build") {
            dependsOn(named("shadowJar"))
        }
    }
}

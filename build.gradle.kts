import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import java.net.URI
import com.diffplug.gradle.spotless.SpotlessPlugin

plugins {
    java
    `java-library`
    `maven-publish`
    signing

    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    alias(libs.plugins.grgit)
    alias(libs.plugins.nexus)

    eclipse
    idea
}

group = "com.plotsquared"
version = "6.10.9-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()

        maven {
            name = "Sonatype OSS"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }

        maven {
            name = "Sonatype OSS (S01)"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }

        maven {
            name = "Jitpack"
            url = uri("https://jitpack.io")
            content {
                includeModule("com.github.MilkBowl", "VaultAPI")
            }
        }

        maven {
            name = "EngineHub"
            url = uri("https://maven.enginehub.org/repo/")
        }
    }

    apply {
        plugin<JavaPlugin>()
        plugin<JavaLibraryPlugin>()
        plugin<MavenPublishPlugin>()
        plugin<ShadowPlugin>()
        plugin<SpotlessPlugin>()
        plugin<SigningPlugin>()

        plugin<EclipsePlugin>()
        plugin<IdeaPlugin>()
    }

    dependencies {
        implementation(platform("com.intellectualsites.bom:bom-1.18.x:1.21"))
    }

    dependencies {
        // Tests
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    }

    plugins.withId("java") {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.compileJava.configure {
        options.release.set(17)
    }

    configurations.all {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
    }

    spotless {
        java {
            licenseHeaderFile(rootProject.file("HEADER.txt"))
            target("**/*.java")
        }
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    val javaComponent = components["java"] as AdhocComponentWithVariants
    javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
        skip()
    }

    signing {
        if (!version.toString().endsWith("-SNAPSHOT")) {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
            signing.isRequired
            sign(publishing.publications)
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {

                    name.set(project.name + " " + project.version)
                    description.set("PlotSquared, a land and world management plugin for Minecraft.")
                    url.set("https://github.com/IntellectualSites/PlotSquared")

                    licenses {
                        license {
                            name.set("GNU General Public License, Version 3.0")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("Sauilitired")
                            name.set("Alexander Söderberg")
                            organization.set("IntellectualSites")
                        }
                        developer {
                            id.set("NotMyFault")
                            name.set("Alexander Brandes")
                            organization.set("IntellectualSites")
                            email.set("contact@notmyfault.dev")
                        }
                        developer {
                            id.set("SirYwell")
                            name.set("Hannes Greule")
                            organization.set("IntellectualSites")
                        }
                        developer {
                            id.set("dordsor21")
                            name.set("dordsor21")
                            organization.set("IntellectualSites")
                        }
                    }

                    scm {
                        url.set("https://github.com/IntellectualSites/PlotSquared")
                        connection.set("scm:https://IntellectualSites@github.com/IntellectualSites/PlotSquared.git")
                        developerConnection.set("scm:git://github.com/IntellectualSites/PlotSquared.git")
                    }

                    issueManagement {
                        system.set("GitHub")
                        url.set("https://github.com/IntellectualSites/PlotSquared/issues")
                    }
                }
            }
        }
    }

    tasks {

        compileJava {
            options.compilerArgs.add("-parameters")
            options.isDeprecation = true
            options.encoding = "UTF-8"
        }

        shadowJar {
            this.archiveClassifier.set(null as String?)
            this.archiveFileName.set("${project.name}-${project.version}.${this.archiveExtension.getOrElse("jar")}")
        }

        named("build") {
            dependsOn(named("shadowJar"))
        }
        test {
            useJUnitPlatform()
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(URI.create("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(URI.create("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

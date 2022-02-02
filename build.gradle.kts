import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.cadixdev.gradle.licenser.LicenseExtension
import org.cadixdev.gradle.licenser.Licenser
import java.net.URI

plugins {
    java
    `java-library`
    `maven-publish`
    signing

    alias(libs.plugins.shadow)
    alias(libs.plugins.licenser)
    alias(libs.plugins.grgit)
    alias(libs.plugins.nexus)

    eclipse
    idea
}

version = "6.4.1-SNAPSHOT"

allprojects {
    group = "com.plotsquared"
    version = rootProject.version

    repositories {
        mavenCentral()

        maven {
            name = "Sonatype OSS"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }

        maven {
            name = "Sonatype OSS (S01)"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
        }

        maven {
            name = "Jitpack"
            url = uri("https://jitpack.io")
        }

        maven {
            name = "EngineHub"
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
        plugin<SigningPlugin>()

        plugin<EclipsePlugin>()
        plugin<IdeaPlugin>()
    }
}

val javadocDir = rootDir.resolve("docs").resolve("javadoc").resolve(project.name)
allprojects {
    dependencies {
        // Tests
        testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
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

    configure<LicenseExtension> {
        header(rootProject.file("HEADER.txt"))
        include("**/*.java")
        newLine.set(false)
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
                    description.set("PlotSquared is a land and world management plugin for Minecraft.")
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
                            name.set("NotMyFault")
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

                    issueManagement{
                        system.set("GitHub")
                        url.set("https://github.com/IntellectualSites/PlotSquared/issues")
                    }
                }
            }
        }
    }

    tasks {
        named<Delete>("clean") {
            doFirst {
                javadocDir.deleteRecursively()
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
        }

        shadowJar {
            this.archiveClassifier.set(null as String?)
            this.archiveFileName.set("${project.name}-${project.version}.${this.archiveExtension.getOrElse("jar")}")
            this.destinationDirectory.set(rootProject.tasks.shadowJar.get().destinationDirectory.get())
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

tasks {
    val aggregatedJavadocs = create<Javadoc>("aggregatedJavadocs") {
        title = "${project.name} ${project.version} API"
        setDestinationDir(javadocDir)
        options.destinationDirectory = javadocDir

        doFirst {
            javadocDir.deleteRecursively()
        }
    }.also {
        it.group = "Documentation"
        it.description = "Generate javadocs from all child projects as if it was a single project"
    }

    subprojects.forEach { subProject ->
        subProject.afterEvaluate {
            subProject.tasks.withType<Javadoc>().forEach { task ->
                aggregatedJavadocs.source += task.source
                aggregatedJavadocs.classpath += task.classpath
                aggregatedJavadocs.excludes += task.excludes
                aggregatedJavadocs.includes += task.includes

                val rootOptions = aggregatedJavadocs.options as StandardJavadocDocletOptions
                val subOptions = task.options as StandardJavadocDocletOptions
                rootOptions.links(*subOptions.links.orEmpty().minus(rootOptions.links.orEmpty().toSet()).toTypedArray())
            }
        }
    }

    build {
        dependsOn(aggregatedJavadocs)
    }
}

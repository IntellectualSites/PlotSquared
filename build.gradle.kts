import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.cadixdev.gradle.licenser.LicenseExtension
import org.cadixdev.gradle.licenser.Licenser

plugins {
    java
    `java-library`
    `maven-publish`
    signing

    alias(libs.plugins.shadow)
    alias(libs.plugins.licenser)
    alias(libs.plugins.grgit)

    eclipse
    idea
}

var ver by extra("6.1.3")
var versuffix by extra("-SNAPSHOT")
val versionsuffix: String? by project
if (versionsuffix != null) {
    versuffix = "-$versionsuffix"
}
version = if (!project.hasProperty("release")) {
    ver + versuffix
} else {
    ver
}

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
            name = "Jitpack"
            url = uri("https://jitpack.io")
        }

        maven {
            name = "IntellectualSites"
            url = uri("https://mvn.intellectualsites.com/content/groups/public/")
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
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
    }

    plugins.withId("java") {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(16))
        }
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
            signing.isRequired
            sign(publishing.publications)
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                // This includes not only the original jar (i.e. not shadowJar),
                // but also sources & javadocs due to the above java block.
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
                        }
                        developer {
                            id.set("NotMyFault")
                            name.set("NotMyFault")
                        }
                        developer {
                            id.set("SirYwell")
                            name.set("Hannes Greule")
                        }
                        developer {
                            id.set("dordsor21")
                            name.set("dordsor21")
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
                    val releasesRepositoryUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val snapshotRepositoryUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                    url = uri(
                            if (version.toString().endsWith("-SNAPSHOT")) snapshotRepositoryUrl
                            else releasesRepositoryUrl
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

        jar {
            this.archiveClassifier.set("jar")
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
                rootOptions.links(*subOptions.links.orEmpty().minus(rootOptions.links.orEmpty()).toTypedArray())
            }
        }
    }

    build {
        dependsOn(aggregatedJavadocs)
    }
}

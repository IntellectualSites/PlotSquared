import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.*

val Project.sourceSets: SourceSetContainer
    get() = the<JavaPluginConvention>().sourceSets

fun Project.applyTasksConfiguration() {
    applyGeneralConfiguration()
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "eclipse")
    apply(plugin = "idea")
    apply(plugin = "com.github.johnrengelman.shadow")

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks
            .withType<JavaCompile>()
            .matching { it.name == "compileJava" || it.name == "compileTestJava" }
            .configureEach {
                val disabledLint = listOf(
                        "processing", "path", "fallthrough", "serial"
                )
                options.compilerArgs.addAll(listOf("-Xlint:all") + disabledLint.map { "-Xlint:-$it" })
                options.isDeprecation = true
                options.encoding = "UTF-8"
            }

    /*
    //Commenting this out until our checkstyle is done
    configure<CheckstyleExtension> {
        configFile = rootProject.file("checkstyle.xml")
        toolVersion = "Figure a tool version that matches our needs"
    }
     */

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        "testImplementation"("junit:junit:${Versions.JUNIT}")
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

    tasks.named("assemble").configure {
        dependsOn("javadocJar")
    }

    artifacts {
        add("archives", tasks.named("jar"))
        add("archives", tasks.named("javadocJar"))
    }

    if (name == "PlotSquared-Core" || name == "PlotSquared-Bukkit") {
        tasks.register<Jar>("sourcesJar") {
            dependsOn("classes")
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        artifacts {
            add("archives", tasks.named("sourcesJar"))
        }
        tasks.named("assemble").configure {
            dependsOn("sourcesJar")
        }
    }

    /*
    tasks.named("check").configure {
        dependsOn("checkstyleMain", "checkstyleTest")
    }

     */
}
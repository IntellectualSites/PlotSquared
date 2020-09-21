import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.repositories

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
    }

    apply(plugin = "net.minecrell.licenser")
    configure<LicenseExtension> {
        header = rootProject.file("HEADER.txt")
        include("**/*.java")
    }
}
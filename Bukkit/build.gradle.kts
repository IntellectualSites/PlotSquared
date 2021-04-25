import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven {
        name = "SpigotMC"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        name = "PlaceholderAPI"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        name = "PaperMC"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        name = "EssentialsX"
        url = uri("https://ci.ender.zone/plugin/repository/everything/")
    }

    maven {
        name = "IntellectualSites Releases"
        url = uri("https://mvn.intellectualsites.com/content/repositories/releases")
    }
    maven {
        name = "IntellectualSites 3rd Party"
        url = uri("https://mvn.intellectualsites.com/content/repositories/thirdparty")
        content {
            includeGroup("de.notmyfault")
        }
    }
}

dependencies {
    api(projects.plotSquaredCore)

    // Metrics
    implementation(libs.bstats)

    // Paper
    compileOnlyApi(libs.paper)
    implementation(libs.paperlib)

    // Plugins
    compileOnly(libs.worldeditBukkit) {
        exclude(group = "org.bukkit")
        exclude(group = "org.spigotmc")
    }
    compileOnly(libs.vault) {
        exclude(group = "org.bukkit")
    }
    compileOnly(libs.placeholderapi)
    compileOnly(libs.luckperms)
    compileOnly(libs.essentialsx)
    compileOnly(libs.hyperverse) { isTransitive = false }
    compileOnly(libs.mvdwapi) { isTransitive = false }

    // Other libraries
    implementation(libs.squirrelid) { isTransitive = false }
    implementation(libs.serverlib)

    // Our libraries
    implementation(libs.arkitektonika)
    implementation(libs.http4j)
    implementation(libs.paster)

    // Adventure
    implementation(libs.platform)
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        exclude(dependency("org.checkerframework:"))
    }

    relocate("net.kyori.adventure", "com.plotsquared.core.configuration.adventure")
    relocate("net.kyori.examination", "com.plotsquared.core.configuration.examination")
    relocate("io.papermc.lib", "com.plotsquared.bukkit.paperlib")
    relocate("org.bstats", "com.plotsquared.metrics")
    relocate("com.sk89q.squirrelid", "com.plotsquared.squirrelid")
    relocate("org.khelekore.prtree", "com.plotsquared.prtree")
    relocate("org.apache.logging.log4j", "com.plotsquared.logging.apache.log4j")
    relocate("org.apache.logging.slf4j", "com.plotsquared.logging.apache.slf4j")
    relocate("org.slf4j", "com.plotsquared.logging.slf4j")
    relocate("com.google.inject", "com.plotsquared.google")
    relocate("org.aopalliance", "com.plotsquared.core.aopalliance")
    relocate("com.intellectualsites.services", "com.plotsquared.core.services")
    relocate("com.intellectualsites.arkitektonika", "com.plotsquared.core.arkitektonika")
    relocate("com.intellectualsites.http", "com.plotsquared.core.http")
    relocate("com.intellectualsites.paster", "com.plotsquared.core.paster")
    relocate("de.notmyfault:serverlib", "com.plotsquared.bukkit.serverlib")

    // Get rid of all the libs which are 100% unused.
    minimize()

    mergeServiceFiles()
}

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
        url = uri("https://repo.essentialsx.net/releases/")
    }

    maven {
        name = "IntellectualSites Releases"
        url = uri("https://mvn.intellectualsites.com/content/repositories/releases")
    }
    maven {
        name = "IntellectualSites 3rd Party"
        url = uri("https://mvn.intellectualsites.com/content/repositories/thirdparty")
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
    relocate("com.google.inject", "com.plotsquared.google")
    relocate("org.aopalliance", "com.plotsquared.core.aopalliance")
    relocate("com.intellectualsites.services", "com.plotsquared.core.services")
    relocate("com.intellectualsites.arkitektonika", "com.plotsquared.core.arkitektonika")
    relocate("com.intellectualsites.http", "com.plotsquared.core.http")
    relocate("com.intellectualsites.paster", "com.plotsquared.core.paster")
    relocate("org.incendo.serverlib", "com.plotsquared.bukkit.serverlib")

    // Get rid of all the libs which are 100% unused.
    minimize()

    mergeServiceFiles()
}

tasks {
    withType<Javadoc> {
        val opt = options as StandardJavadocDocletOptions
        opt.links("https://papermc.io/javadocs/paper/1.17/")
        opt.links("https://docs.enginehub.org/javadoc/com.sk89q.worldedit/worldedit-core/7.2.6-SNAPSHOT/")
        opt.links("https://docs.enginehub.org/javadoc/com.sk89q.worldedit/worldedit-bukkit/7.2.6-SNAPSHOT/")
        opt.links("https://jd.adventure.kyori.net/api/4.8.1/")
        opt.links("https://google.github.io/guice/api-docs/5.0.1/javadoc/")
        opt.links("https://checkerframework.org/api/")
    }
}

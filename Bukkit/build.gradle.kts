import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
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
}

dependencies {
    api(projects.plotSquaredCore)

    // Metrics
    implementation(libs.bstats)

    // Paper
    compileOnly(libs.paper)
    implementation(libs.paperlib)

    // Plugins
    compileOnly(libs.worldeditBukkit) {
        exclude(group = "org.bukkit")
        exclude(group = "org.spigotmc")
    }
    compileOnly(libs.fastasyncworldeditBukkit) { isTransitive = false }
    testImplementation(libs.fastasyncworldeditBukkit) { isTransitive = false }
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
    implementation(libs.adventurePlatformBukkit)
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
    relocate("org.enginehub", "com.plotsquared.squirrelid")
    relocate("org.khelekore.prtree", "com.plotsquared.prtree")
    relocate("com.google.inject", "com.plotsquared.google")
    relocate("org.aopalliance", "com.plotsquared.core.aopalliance")
    relocate("com.intellectualsites.services", "com.plotsquared.core.services")
    relocate("com.intellectualsites.arkitektonika", "com.plotsquared.core.arkitektonika")
    relocate("com.intellectualsites.http", "com.plotsquared.core.http")
    relocate("com.intellectualsites.paster", "com.plotsquared.core.paster")
    relocate("org.incendo.serverlib", "com.plotsquared.bukkit.serverlib")
    relocate("org.jetbrains", "com.plotsquared.core.annotations")
    relocate("org.intellij.lang", "com.plotsquared.core.intellij.annotations")
    relocate("javax.annotation", "com.plotsquared.core.annotation")
    relocate("javax.inject", "com.plotsquared.core.annotation.inject")

    // Get rid of all the libs which are 100% unused.
    minimize()

    mergeServiceFiles()
}

tasks {
    withType<Javadoc> {
        val opt = options as StandardJavadocDocletOptions
        opt.links("https://papermc.io/javadocs/paper/1.17/")
        opt.links("https://docs.enginehub.org/javadoc/com.sk89q.worldedit/worldedit-core/7.2.6/")
        opt.links("https://docs.enginehub.org/javadoc/com.sk89q.worldedit/worldedit-bukkit/7.2.6/")
        opt.links("https://jd.adventure.kyori.net/api/4.9.1/")
        opt.links("https://google.github.io/guice/api-docs/5.0.1/javadoc/")
        opt.links("https://checkerframework.org/api/")
    }
}

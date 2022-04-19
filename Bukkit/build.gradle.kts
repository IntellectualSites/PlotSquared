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
    relocate("io.papermc.lib", "com.plotsquared.bukkit.internal.paperlib")
    relocate("org.bstats", "com.plotsquared.core.internal.metrics")
    relocate("org.enginehub", "com.plotsquared.core.squirrelid")
    relocate("org.khelekore.prtree", "com.plotsquared.core.prtree")
    relocate("com.google.inject", "com.plotsquared.core.google")
    relocate("org.aopalliance", "com.plotsquared.core.google.aopalliance")
    relocate("cloud.commandframework.services", "com.plotsquared.core.cloud.services")
    relocate("io.leangen.geantyref", "com.plotsquared.core.cloud.geantyref")
    relocate("com.intellectualsites.arkitektonika", "com.plotsquared.core.web.arkitektonika")
    relocate("com.intellectualsites.http", "com.plotsquared.core.web.http")
    relocate("com.intellectualsites.paster", "com.plotsquared.core.web.paster")
    relocate("org.incendo.serverlib", "com.plotsquared.bukkit.internal.serverlib")
    relocate("org.jetbrains", "com.plotsquared.core.annotations.jetbrains.annotation")
    relocate("org.intellij.lang", "com.plotsquared.core.annotations.intellij.annotations")
    relocate("javax.annotation", "com.plotsquared.core.annotations.javax.annotation")
    relocate("com.github.spotbugs", "com.plotsquared.core.spotbugs")
    relocate("javax.inject", "com.plotsquared.core.annotations.javax.inject")
    relocate("net.jcip", "com.plotsquared.core.annotations.jcip")
    relocate("edu.umd.cs.findbugs", "com.plotsquared.core.annotations.findbugs")

    // Get rid of all the libs which are 100% unused.
    minimize()

    mergeServiceFiles()
}

tasks {
    withType<Javadoc> {
        val opt = options as StandardJavadocDocletOptions
        opt.links("https://papermc.io/javadocs/paper/1.18/")
        opt.links("https://docs.enginehub.org/javadoc/com.sk89q.worldedit/worldedit-bukkit/" + libs.worldeditBukkit.get().versionConstraint.toString())
        opt.links("https://javadoc.io/doc/com.plotsquared/PlotSquared-Core/latest/")
        opt.links("https://jd.adventure.kyori.net/api/" + libs.adventure.get().versionConstraint.toString())
        opt.links("https://google.github.io/guice/api-docs/" + libs.guice.get().versionConstraint.toString() + "/javadoc/")
        opt.links("https://checkerframework.org/api/")
    }
}

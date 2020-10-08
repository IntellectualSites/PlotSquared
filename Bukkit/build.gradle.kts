import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven {
        name = "SpigotMC"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.org/repository/maven-public")
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
        name = "IntellectualSites"
        url = uri("https://mvn.intellectualsites.com/content/repositories/releases")
    }
}

dependencies {
    api(project(":PlotSquared-Core"))

    //
    // Implementation details
    //

    // ~~Spyware~~ Metrics
    implementation("org.bstats:bstats-bukkit:1.7")

    // Minecraft
    compileOnlyApi("com.destroystokyo.paper:paper-api:1.16.3-R0.1-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.4")

    // Plugins
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.0-SNAPSHOT") {
        exclude(group = "org.bukkit")
        exclude(group = "org.spigotmc")
    }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit")
    }
    compileOnly("me.clip:placeholderapi:2.10.6")
    compileOnly("net.luckperms:api:5.1")
    compileOnly("net.ess3:EssentialsX:2.18.0")
    compileOnly("se.hyperver.hyperverse:Core:0.6.0-SNAPSHOT") { isTransitive = false }
    compileOnly("be.maximvdw:MVdWPlaceholderAPI:3.1.1") { isTransitive = false }

    // Other libraries
    implementation("com.sk89q:squirrelid:1.0.0-SNAPSHOT") { isTransitive = false }

    // Adventure
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
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
    relocate("org.apache.logging.slf4j", "com.plotsquared.logging.apache")
    relocate("org.slf4j", "com.plotsquared.logging.slf4j")
    relocate("com.google.inject", "com.plotsquared.google")
    relocate("javax.inject", "com.plotsquared.core.inject.javax")
    relocate("org.aopalliance", "com.plotsquared.core.aopalliance")
    relocate("com.intellectualsites.services", "com.plotsquared.core.services")

    // Get rid of all the libs which are 100% unused.
    minimize()
}

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
    compileOnly("com.sk89q:squirrelid:1.0.0-SNAPSHOT") { isTransitive = false }

    // Adventure
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
}

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

// TODO: Get this to be not ugly...
tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include(dependency(":PlotSquared-Core"))
        include(dependency("io.papermc:paperlib"))
        include(dependency("net.kyori:adventure-platform-bukkit"))
        include(dependency("net.kyori:adventure-text-minimessage"))
        include(dependency("net.kyori:adventure-text-serializer-bungeecord"))
        include(dependency("net.kyori:adventure-text-serializer-legacy"))
        include(dependency("net.kyori:adventure-text-serializer-gson"))
        include(dependency("net.kyori:adventure-api"))
        include(dependency("net.kyori:adventure-platform-api"))
        include(dependency("net.kyori:adventure-platform-common"))
        include(dependency("net.kyori:adventure-platform-viaversion"))
        include(dependency("net.kyori:adventure-nbt"))
        include(dependency("net.kyori:examination-api"))
        include(dependency("net.kyori:examination-string"))
        include(dependency("org.bstats:bstats-bukkit"))
        include(dependency("org.khelekore:prtree"))
        include(dependency("com.sk89q:squirrelid"))
        include(dependency("com.google.inject:guice"))
        include(dependency("com.google.inject.extensions:guice-assistedinject"))
        include(dependency("javax.annotation:javax-annotation-api"))
        include(dependency("org.apache.logging.log4j:log4j-slf4j-impl"))
        include(dependency("org.slf4j:slf4j-api"))
        include(dependency("javax.inject:javax.inject"))
        include(dependency("aopalliance:aopalliance"))
        include(dependency("com.intellectualsites:Pipeline"))

        relocate("net.kyori.adventure", "com.plotsquared.core.configuration.adventure")
        relocate("io.papermc.lib", "com.plotsquared.bukkit.paperlib")
        relocate("org.bstats", "com.plotsquared.metrics")
        relocate("com.sk89q.squirrelid", "com.plotsquared.squirrelid")
        relocate("org.khelekore.prtree", "com.plotsquared.prtree")
        relocate("org.apache.logging.slf4j", "com.plotsquared.logging.apache")
        relocate("org.slf4j", "com.plotsquared.logging.slf4j")
        relocate("com.google.inject", "com.plotsquared.google")
        relocate("javax.inject", "com.plotsquared.core.inject.javax")
        relocate("org.json", "com.plotsquared.json") {
            exclude("org/json/simple/**")
        }
    }
}

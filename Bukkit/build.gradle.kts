import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyTasksConfiguration()

repositories {
    mavenLocal()
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
        name = "BungeePerms"
        url = uri("https://repo.wea-ondara.net/repository/public/")
    }
    maven {
        name = "FeatherBoard"
        url = uri("http://repo.mvdw-software.be/content/groups/public/")
    }
}


dependencies {
    "compileOnly"(project(":PlotSquared-Core"))
    "compileOnly"("org.bstats:bstats-bukkit:1.7")
    "compileOnly"(project(":PlotSquared-Core"))
    "compileOnly"("org.spigotmc:spigot-api:1.16.3-R0.1-SNAPSHOT")
    "compileOnly"("com.destroystokyo.paper:paper-api:1.16.3-R0.1-SNAPSHOT")
    "compileOnly"("com.sk89q.worldedit:worldedit-bukkit:7.2.0-SNAPSHOT") {
        exclude(group = "bukkit")
    }
    "compileOnly"("io.papermc:paperlib:1.0.4")
    "implementation"("net.kyori:text-adapter-bukkit:3.0.3")
    "compileOnly"("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "bukkit")
    }
    "implementation"("me.clip:placeholderapi:2.10.6")
    "implementation"("net.luckperms:api:5.1")
    "implementation"("net.ess3:EssentialsX:2.18.0")
    "implementation"("net.alpenblock:BungeePerms:4.0-dev-106")
    "implementation"("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
    "implementation"("net.kyori:adventure-text-minimessage:4.0.0-SNAPSHOT")
    "compileOnly"("se.hyperver.hyperverse:Core:0.6.0-SNAPSHOT") { isTransitive = false }
    "compileOnly"("com.sk89q:squirrelid:1.0.0-SNAPSHOT") { isTransitive = false }
    "compileOnly"("be.maximvdw:MVdWPlaceholderAPI:3.1.1-SNAPSHOT") { isTransitive = false }
    // logging
    "implementation"("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")
}

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include(dependency(":PlotSquared-Core"))
        include(dependency("io.papermc:paperlib:1.0.4"))
        include(dependency("net.kyori:adventure-platform-bukkit:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-text-minimessage:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-text-serializer-bungeecord:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-text-serializer-legacy:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-text-serializer-gson:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-api:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-platform-api:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-platform-common:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-platform-viaversion:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-nbt:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:examination-api:1.0.0"))
        include(dependency("net.kyori:examination-string:1.0.0"))
        include(dependency("org.bstats:bstats-bukkit:1.7"))
        include(dependency("org.khelekore:prtree:1.7.0-SNAPSHOT"))
        include(dependency("com.sk89q:squirrelid:1.0.0-SNAPSHOT"))
        include(dependency("com.google.inject:guice:4.2.3"))
        include(dependency("com.google.inject.extensions:guice-assistedinject:4.2.3"))
        include(dependency("javax.annotation:javax-annotation-api"))
        include(dependency("org.apache.logging.log4j:log4j-slf4j-impl"))
        include(dependency("org.slf4j:slf4j-api"))
        include(dependency("javax.inject:javax.inject:1"))
        include(dependency("aopalliance:aopalliance:1.0"))
        include(dependency("com.intellectualsites:Pipeline:1.4.0-SNAPSHOT"))
        relocate("net.kyori.adventure", "com.plotsquared.core.configuration.adventure")
        relocate("io.papermc.lib", "com.plotsquared.bukkit.paperlib")
        relocate("org.bstats", "com.plotsquared.metrics")
        relocate("com.sk89q.squirrelid", "com.plotsquared.squirrelid")
        relocate("org.khelekore.prtree", "com.plotsquared.prtree")
        relocate("org.apache.logging.slf4j", "com.plotsquared.logging.apache")
        relocate("org.slf4j", "com.plotsquared.logging.slf4j")
        relocate("com.google.inject", "com.plotsquared.google")
        relocate("javax.inject", "com.plotsquared.core.inject.javax")

        archiveFileName.set("${project.name}-${project.version}.jar")
        destinationDirectory.set(file("../target"))
    }
}

tasks.named("build").configure {
    dependsOn("shadowJar")
}
import java.time.format.DateTimeFormatter

dependencies {
    // Expected everywhere.
    compileOnlyApi("javax.annotation:javax.annotation-api:1.3.2")

    // Minecraft expectations
    compileOnlyApi("com.google.guava:guava:21.0") // Minecraft uses v21.0
    compileOnlyApi("com.google.code.gson:gson:2.8.0") // Minecraft uses v2.8.0

    // Platform expectations
    compileOnlyApi("org.yaml:snakeyaml:1.26") // Some platforms provide this

    // Adventure stuff
    api("net.kyori:adventure-api:4.1.1")
    api("net.kyori:adventure-text-minimessage:4.0.0-SNAPSHOT")

    // Guice
    api("com.google.inject:guice:4.2.3") {
        exclude(group = "com.google.guava")
    }
    api("com.google.inject.extensions:guice-assistedinject:4.2.3") {
        exclude("com.google.inject", "guice")
    }
    compileOnlyApi("com.google.code.findbugs:annotations:3.0.1")
    compileOnlyApi("javax.inject:javax.inject:1")

    // Plugins
    compileOnlyApi("com.sk89q.worldedit:worldedit-core:7.2.0") {
        exclude(group = "bukkit-classloader-check")
        exclude(group = "mockito-core")
        exclude(group = "dummypermscompat")
    }
    testImplementation("com.sk89q.worldedit:worldedit-core:7.2.0")

    // Logging
    api("org.slf4j:slf4j-api:1.7.25")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1") {
        exclude(group = "org.slf4j")
    }

    // Other libraries
    api("org.khelekore:prtree:1.7.0-SNAPSHOT")
    api("aopalliance:aopalliance:1.0")
    api("com.intellectualsites:Pipeline:1.4.0-SNAPSHOT") {
        exclude(group = "com.google.guava")
    }
}

tasks.processResources {
    filesMatching("plugin.properties") {
        expand(
            "version" to project.version.toString(),
            "commit" to rootProject.grgit.head().abbreviatedId,
            "date" to rootProject.grgit.head().dateTime.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
        )
    }
}

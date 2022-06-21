import java.time.format.DateTimeFormatter

dependencies {
    // Expected everywhere.
    compileOnlyApi("org.checkerframework:checker-qual")

    // Minecraft expectations
    compileOnlyApi("com.google.code.gson:gson")
    compileOnly("com.google.guava:guava")

    // Platform expectations
    compileOnlyApi("org.yaml:snakeyaml")

    // Adventure
    api("net.kyori:adventure-api")
    api("net.kyori:adventure-text-minimessage")

    // Guice
    api(libs.guice) {
        exclude(group = "com.google.guava")
    }
    api(libs.guiceassistedinject) {
        exclude("com.google.inject", "guice")
    }
    api(libs.spotbugs)

    // Plugins
    compileOnly(libs.worldeditCore) {
        exclude(group = "bukkit-classloader-check")
        exclude(group = "mockito-core")
        exclude(group = "dummypermscompat")
    }
    testImplementation(libs.worldeditCore)
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core") { isTransitive = false }
    testImplementation("com.fastasyncworldedit:FastAsyncWorldEdit-Core") { isTransitive = false }

    // Logging
    compileOnlyApi("org.apache.logging.log4j:log4j-api")

    // Other libraries
    api(libs.prtree)
    api(libs.aopalliance)
    api(libs.cloudServices)
    api(libs.arkitektonika)
    api("com.intellectualsites.paster:Paster")
    api("com.intellectualsites.informative-annotations:informative-annotations")
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

tasks {
    withType<Javadoc> {
        val opt = options as StandardJavadocDocletOptions
        opt.links("https://docs.enginehub.org/javadoc/com.sk89q.worldedit/worldedit-core/" + libs.worldeditCore.get().versionConstraint.toString())
        opt.links("https://jd.adventure.kyori.net/api/4.9.3/")
        opt.links("https://google.github.io/guice/api-docs/" + libs.guice.get().versionConstraint.toString() + "/javadoc/")
        opt.links("https://checkerframework.org/api/")
        opt.links("https://javadoc.io/doc/com.intellectualsites.informative-annotations/informative-annotations/latest/")
    }
}

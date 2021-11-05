import java.time.format.DateTimeFormatter

dependencies {
    // Expected everywhere.
    compileOnlyApi(libs.checkerqual)

    // Minecraft expectations
    compileOnlyApi(libs.guava)
    compileOnlyApi(libs.gson)

    // Platform expectations
    compileOnlyApi(libs.snakeyaml)

    // Adventure
    api(libs.adventure)
    api(libs.minimessage)

    // Guice
    api(libs.guice) {
        exclude(group = "com.google.guava")
    }
    api(libs.guiceassistedinject) {
        exclude("com.google.inject", "guice")
    }
    compileOnlyApi(libs.findbugs)

    // Plugins
    compileOnly(libs.worldeditCore) {
        exclude(group = "bukkit-classloader-check")
        exclude(group = "mockito-core")
        exclude(group = "dummypermscompat")
    }
    testImplementation(libs.worldeditCore)
    compileOnly(libs.fastasyncworldeditCore) { isTransitive = false }
    testImplementation(libs.fastasyncworldeditCore) { isTransitive = false }

    // Logging
    compileOnlyApi(libs.log4j)

    // Other libraries
    api(libs.prtree)
    api(libs.aopalliance)
    api(libs.pipeline) {
        exclude(group = "com.google.guava")
    }
    api(libs.arkitektonika)
    api(libs.paster)
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

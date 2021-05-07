import java.time.format.DateTimeFormatter

dependencies {
    // Expected everywhere.
    compileOnlyApi(libs.checkerqual)

    // Minecraft expectations
    compileOnlyApi(libs.guava) {
        because("Minecraft uses 21.0")
    }
    compileOnlyApi(libs.gson) {
        because("Minecraft uses 2.8.0")
    }

    // Platform expectations
    compileOnlyApi(libs.snakeyaml) {
        because("Bukkit uses 1.27")
    }

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
    compileOnlyApi(libs.worldeditCore) {
        exclude(group = "bukkit-classloader-check")
        exclude(group = "mockito-core")
        exclude(group = "dummypermscompat")
    }
    testImplementation(libs.worldeditCore)

    // Logging
    api(libs.slf4j)
    runtimeOnly(libs.log4j) {
        exclude(group = "org.slf4j")
        because("Minecraft uses 2.8.1")
    }

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

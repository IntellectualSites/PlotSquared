rootProject.name = "PlotSquared"

include("Core", "Bukkit")

project(":Core").name = "plotsquared-core"
project(":Bukkit").name = "plotsquared-bukkit"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "jmp repository"
            url = uri("https://repo.jpenilla.xyz/snapshots")
        }
    }
}

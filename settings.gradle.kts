rootProject.name = "PlotSquared"

include("Core", "Bukkit", "Sponge")

project(":Core").name = "PlotSquared-Core"
project(":Bukkit").name = "PlotSquared-Bukkit"
project(":Sponge").name = "PlotSquared-Sponge"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

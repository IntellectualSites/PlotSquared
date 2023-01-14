plugins {
    id("com.gradle.enterprise") version("3.9")
}

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}

rootProject.name = "PlotSquared"

include("Core", "Bukkit")

project(":Core").name = "PlotSquared-Core"
project(":Bukkit").name = "PlotSquared-Bukkit"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

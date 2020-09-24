plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    jcenter()
    gradlePluginPortal()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

dependencies {
    implementation(gradleApi())
    implementation("gradle.plugin.net.minecrell:licenser:0.4.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:4.1.0")
    implementation("com.github.jengelman.gradle.plugins:shadow:6.0.0")
}
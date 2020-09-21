import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyTasksConfiguration()

dependencies {
    "implementation"("org.yaml:snakeyaml:1.26")
    "implementation"("com.google.code.gson:gson:${Versions.GSON}")
    "implementation"("org.jetbrains.kotlin:kotlin-stdlib:1.3.72")
    "implementation"("org.khelekore:prtree:1.7.0-SNAPSHOT")
    // Adventure related stuff
    "implementation"("net.kyori:adventure-api:${Versions.ADVENTURE}")
    "implementation"("net.kyori:adventure-text-minimessage:${Versions.ADVENTURE}")
    "compileOnly"("com.google.inject:guice:4.2.3")
    "compileOnly"("com.google.inject.extensions:guice-assistedinject:4.2.3")
    "compileOnly"("com.google.code.findbugs:annotations:3.0.1")
    "compileOnly"("javax.inject:javax.inject:1")
    "compileOnly"("aopalliance:aopalliance:1.0")
    // logging
    "implementation"("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")
    "implementation"("com.intellectualsites:Pipeline:1.4.0-SNAPSHOT")
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include(dependency("net.kyori:adventure-api:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-gson:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-legacy:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-plain:${Versions.ADVENTURE}"))
        include(dependency("net.kyori:adventure-text-minimessage:${Versions.ADVENTURE}"))
        include(dependency("org.khelekore:prtree:1.7.0-SNAPSHOT"))
        relocate("net.kyori.text", "com.plotsquared.formatting.text")
        relocate("org.json", "com.plotsquared.json") {
            exclude("org/json/simple/**")
        }
    }
}

sourceSets.named("main") {
    java {
        srcDir("src/main/java")
    }
    resources {
        srcDir("src/main/resources")
    }
}

tasks.named("build").configure {
    dependsOn("shadowJar")
}
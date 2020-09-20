dependencies {
    // Expected everywhere.
    api("org.json:json:20200518")
    api("javax.annotation:javax.annotation-api:1.3.2")

    // Minecraft expectations
    compileOnlyApi("com.google.guava:guava:21.0") // Minecraft uses v21.0
    compileOnlyApi("com.google.code.gson:gson:2.8.0") // Minecraft uses v2.8.0

    // Platform expectations
    compileOnlyApi("org.yaml:snakeyaml:1.26") // Some platforms provide this

    // Adventure stuff
    api("net.kyori:adventure-api:4.0.0-SNAPSHOT")
    api("net.kyori:adventure-text-minimessage:4.0.0-SNAPSHOT")

    // Guice
    api("com.google.inject:guice:4.2.3")
    api("com.google.inject.extensions:guice-assistedinject:4.2.3")
    compileOnlyApi("com.google.code.findbugs:annotations:3.0.1")
    compileOnlyApi("javax.inject:javax.inject:1")

    // Logging
    api("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")

    // Other libraries
    api("org.khelekore:prtree:1.7.0-SNAPSHOT")
    api("aopalliance:aopalliance:1.0")
    api("com.intellectualsites:Pipeline:1.4.0-SNAPSHOT")
}

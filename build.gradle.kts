import org.ajoberstar.grgit.Grgit

buildscript {
    configurations.all {
        resolutionStrategy {
            force("org.ow2.asm:asm:8.0.1")
        }
    }
}

plugins {
    id("maven-publish")
}

group = "com.plotsquared"

apply(plugin = "org.ajoberstar.grgit")
ext {
    val git: Grgit = Grgit.open {
        dir = File("$rootDir/.git")
    }
}

var ver by extra("6.0.0")
var versuffix by extra("-SNAPSHOT")
ext {
    if (project.hasProperty("versionsuffix")) {
            versuffix = "-$versionsuffix"
    }
}
version = ver + versuffix

allprojects {
    gradle.projectsEvaluated {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "1000"))
        }
    }
}

tasks.register("clean", Delete::class){
    delete("../target")
}
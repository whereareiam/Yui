import me.whereareiam.attache.plugin.gradle.extension.AttacheMetadataExtension

plugins {
    id("yui.bootstrap-runtime")
    alias(libs.plugins.attache)
}

dependencies {
    attache(libs.jda)
    attache(libs.configura)
    attache(libs.semantica)
}

extensions.configure<AttacheMetadataExtension>("attacheMetadata") {
    repository("https://maven.whereareiam.me/release")
    repository("https://maven.whereareiam.me/development")

    library(libs.jda) {
        transitive.set(true)
    }

    library(libs.configura) {
        transitive.set(true)
    }

    library(libs.semantica) {
        transitive.set(true)
    }
}

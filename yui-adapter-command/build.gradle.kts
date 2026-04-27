import me.whereareiam.attache.plugin.gradle.extension.AttacheMetadataExtension

plugins {
    id("yui.java-common")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.bundles.cloud)
    attache(libs.cloud.core)
    attache(libs.cloud.annotations)
    attache(libs.cloud.cooldown)
    attache(libs.cloud.discord)

    testImplementation(libs.bundles.cloud)
}

extensions.configure<AttacheMetadataExtension>("attacheMetadata") {
    library(libs.cloud.core) {
        transitive.set(true)
    }

    library(libs.cloud.annotations) {
        transitive.set(true)
    }

    library(libs.cloud.cooldown) {
        transitive.set(true)
    }

    library(libs.cloud.discord) {
        transitive.set(true)
    }
}

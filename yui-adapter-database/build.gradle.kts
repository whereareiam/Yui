import me.whereareiam.attache.plugin.gradle.extension.AttacheMetadataExtension

plugins {
    id("yui.java-common")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.spring.boot.data.jpa)
    attache(libs.spring.boot.data.jpa)
    attache(libs.postgres)

    testImplementation(libs.postgres)
    testImplementation(libs.spring.boot.data.jpa)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgres)
}

extensions.configure<AttacheMetadataExtension>("attacheMetadata") {
    library(libs.spring.boot.data.jpa) {
        transitive.set(true)
    }

    library(libs.postgres) {
        transitive.set(true)
    }
}

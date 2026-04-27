import me.whereareiam.attache.plugin.gradle.extension.AttacheMetadataExtension
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("yui.java-common")
    id("yui.provided-api")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.attache)
}

dependencies {
    providedApi(libs.jda)
    providedApi(libs.configura)
    providedApi(libs.semantica)
    providedApi(libs.spring.boot)
    providedApi(libs.spring.boot.webflux)
    api(libs.attache.common)
    api(libs.annotations)
}

buildConfig {
    packageName("me.whereareiam.yui")

    buildConfigField("String", "NAME", "\"${rootProject.name}\"")
    buildConfigField("String", "VERSION", "\"${rootProject.version}\"")

    val catalog = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
    catalog.versionAliases.forEach { alias ->
        val version = catalog.findVersion(alias).get().toString()
        val fieldName = alias.replace("-", "_").replace(".", "_").uppercase()
        buildConfigField("String", fieldName, "\"$version\"")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
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

    library(libs.spring.boot) {
        transitive.set(true)
    }

    library(libs.spring.boot.webflux) {
        transitive.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "Yui"
            pom {
                name.set("Yui")
                description.set("Public API for Yui - Modular Discord Bot Framework")
            }
        }
    }
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        title = "Yui API"
        windowTitle = "Yui API"
    }
}

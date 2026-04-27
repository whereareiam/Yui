import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("yui.java-common")
    alias(libs.plugins.buildconfig)
}

dependencies {
    api(libs.jda)
    api(libs.configura)
    api(libs.semantica)
    api(libs.attache.common)
    api(libs.annotations)
    api(libs.spring.boot)
    api(libs.spring.boot.webflux)
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

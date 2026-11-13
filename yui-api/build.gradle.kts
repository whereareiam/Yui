plugins {
    id("module-api")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.attache)
}

description = "Public API of Yui, the modular Discord bot, that plugins compile against."

// Libraries plugins compile against and Yui provides at runtime: exposed to consumers' compilation and
// declared to Attache, which loads them.
val providedApi = configurations.dependencyScope("providedApi")

configurations.named("compileOnlyApi") {
    extendsFrom(providedApi.get())
}

configurations.named("attacheApi") {
    extendsFrom(providedApi.get())
}

dependencies {
    api(libs.attache.common)
    api(libs.annotations)

    providedApi(libs.jda) {
        // Yui uses no voice: no Opus or NaCl natives.
        exclude(group = "club.minnced", module = "opus-java")
        exclude(group = "com.google.crypto.tink", module = "tink")
    }
    providedApi(libs.configura)
    providedApi(libs.conditia.api)
    providedApi(libs.chassis.api)
    providedApi(libs.conditia.common)
    providedApi(libs.conditia.configura)
    providedApi(libs.semantica.api)
    providedApi(libs.semantica.common)
    providedApi(libs.semantica.icu)
    providedApi(libs.semantica.file)
    providedApi(libs.spring.boot)
    providedApi(libs.spring.boot.data.jpa)
    providedApi(libs.spring.tx)
}

buildConfig {
    packageName("me.whereareiam.yui.api")

    buildConfigField("String", "NAME", "\"${rootProject.name}\"")
    buildConfigField("String", "VERSION", "\"${project.version}\"")

    val catalog = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
    catalog.versionAliases.forEach { alias ->
        val version = catalog.findVersion(alias).get().toString()
        val fieldName = alias.replace("-", "_").replace(".", "_").uppercase()
        buildConfigField("String", fieldName, "\"$version\"")
    }
}

toolkitPublish {
    artifactId.set("api")

    pom {
        name.set("Yui API")
    }
}

attache {
    transitive.set(true)
    repository("https://registry.whereareiam.me/maven/packages")
}

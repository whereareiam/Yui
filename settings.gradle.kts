import org.gradle.api.initialization.resolve.RepositoriesMode

rootProject.name = "Yui"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    // The Gradle plugin that builds Yui's plugins, and any other's.
    includeBuild("yui-gradle-plugin")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://registry.whereareiam.me/maven/packages")
    }
}

plugins {
    // Downloads the Java toolchain the conventions ask for when the machine has none.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()
        maven("https://registry.whereareiam.me/maven/packages")
    }
}

include(":yui-api")
include(":yui-common")
include(":yui-adapter-command")
include(":yui-adapter-database")
include(":yui-adapter-plugin")
include(":yui-launcher")

include(":plugins:plugin-onboarding")
include(":plugins:plugin-onboarding:onboarding-api")
include(":plugins:plugin-interests")

include(":examples:plugin")

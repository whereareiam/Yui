import org.gradle.api.initialization.resolve.RepositoriesMode

rootProject.name = "Yui"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        maven("https://maven.whereareiam.me/release")
        maven("https://maven.whereareiam.me/development")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.whereareiam.me/release")
        maven("https://maven.whereareiam.me/development")
    }
}

include(":yui-adapter-database")
include(":yui-adapter-command")
include(":yui-adapter-plugin")
include(":yui-bootstrap")
include(":yui-common")
include(":yui-api")

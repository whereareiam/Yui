import me.whereareiam.toolkit.versioning.extension.ToolkitVersioningExtension

plugins {
    base
    alias(libs.plugins.toolkit.architecture)
    alias(libs.plugins.toolkit.versioning)
    // Attache keeps its defaults on the root project, so its modules must share one copy of the plugin.
    alias(libs.plugins.attache) apply false
}

// The version comes from the VERSION variable in CI and falls back to yuiVersion locally.
val projectVersion = extensions.getByType<ToolkitVersioningExtension>().apply {
    defaultVersion.set(providers.gradleProperty("yuiVersion"))
}.resolvedVersion().get()

allprojects {
    group = "me.whereareiam.yui"
    version = projectVersion
}

defaultTasks("build")

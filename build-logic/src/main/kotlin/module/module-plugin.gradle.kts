import me.whereareiam.toolkit.architecture.model.ArchitectureExtension

plugins {
    id("module-java")
}

// A plugin of Yui: one jar with its classes, the modules it bundles (its own -api) and yui-plugin.yml, whose
// ${version} becomes the build's version. It compiles against Yui's API, which Yui provides at runtime.
extensions.configure<ArchitectureExtension> {
    kind = assembly
}

val bundled = configurations.dependencyScope("bundled")

configurations.named("implementation") {
    extendsFrom(bundled.get())
}

val bundledClasspath = configurations.resolvable("bundledClasspath") {
    extendsFrom(bundled.get())
    isTransitive = false
}

dependencies {
    "compileOnly"(project(":yui-api"))
    "testImplementation"(project(":yui-api"))
}

tasks.named<ProcessResources>("processResources") {
    val version = project.version.toString()
    inputs.property("version", version)
    filesMatching("yui-plugin.yml") {
        expand("version" to version)
    }
}

tasks.named<Jar>("jar") {
    dependsOn(bundledClasspath)
    from({ bundledClasspath.get().map { zipTree(it) } })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

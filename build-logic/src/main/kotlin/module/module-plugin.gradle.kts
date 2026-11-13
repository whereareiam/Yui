import me.whereareiam.toolkit.architecture.model.ArchitectureExtension

plugins {
    id("module-java")
}

// A plugin of Yui inside this repository. Its project also applies me.whereareiam.yui.plugin, which writes
// yui-plugin.yml and bundles its own modules; here it gets Yui's API, which Yui provides at runtime.
extensions.configure<ArchitectureExtension> {
    kind = assembly
}

dependencies {
    "compileOnly"(project(":yui-api"))
    "testImplementation"(project(":yui-api"))
}

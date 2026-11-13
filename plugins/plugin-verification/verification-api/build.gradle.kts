import me.whereareiam.toolkit.architecture.model.ArchitectureExtension

plugins {
    id("module-java")
}

description = "What other plugins of Yui use of Verification."

extensions.configure<ArchitectureExtension> {
    kind = api
    family = "verification"
}

dependencies {
    api(projects.yuiApi)
}

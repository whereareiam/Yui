import me.whereareiam.toolkit.architecture.model.ArchitectureExtension

plugins {
    id("module-java")
}

description = "What other plugins of Yui use of Onboarding."

extensions.configure<ArchitectureExtension> {
    kind = api
    family = "onboarding"
}

dependencies {
    api(projects.yuiApi)
}

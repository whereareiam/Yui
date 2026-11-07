import me.whereareiam.toolkit.architecture.model.ArchitectureExtension

plugins {
    id("module-java")
}

// Composes the implementations into what is started.
extensions.configure<ArchitectureExtension> {
    kind = assembly
}

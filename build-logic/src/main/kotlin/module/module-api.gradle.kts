import me.whereareiam.toolkit.architecture.model.ArchitectureExtension

plugins {
    id("module-java")
    id("packaging-publication")
}

// Public contracts that plugins compile against; published so that they can.
extensions.configure<ArchitectureExtension> {
    kind = api
}

plugins {
    id("module-java")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(projects.yuiApi)

    attache(libs.cloud.core)
    attache(libs.cloud.annotations)
    attache(libs.cloud.cooldown)
    attache(libs.cloud.discord)

    testImplementation(projects.yuiApi)
    testImplementation(libs.bundles.cloud)
}

attache {
    transitive.set(true)
}

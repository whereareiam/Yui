plugins {
    id("module-java")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(projects.yuiApi)

    attache(libs.chassis.common)
    attache(libs.chassis.spring)
    attache(libs.chassis.attache)
    attache(libs.attache.launcher)

    testImplementation(projects.yuiApi)
    testImplementation(libs.chassis.common)
    testImplementation(libs.chassis.spring)
    testImplementation(libs.chassis.attache)
    testImplementation(libs.chassis.testing)
    testImplementation(libs.attache.launcher)
}

attache {
    transitive.set(true)
}

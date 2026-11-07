plugins {
    id("module-java")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(projects.yuiApi)

    attache(libs.spring.boot.data.jpa)
    attache(libs.postgres)

    testImplementation(projects.yuiApi)
    testImplementation(libs.postgres)
    testImplementation(libs.spring.boot.data.jpa)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgres)
}

attache {
    transitive.set(true)
}

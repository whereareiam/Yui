plugins {
    id("module-java")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(projects.yuiApi)

    attache(libs.spring.boot.data.jpa)
    attache(libs.bundles.database)

    testImplementation(projects.yuiApi)
    testImplementation(libs.spring.boot.data.jpa)
    testImplementation(libs.bundles.database)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgres)
    testImplementation(libs.testcontainers.mariadb)
}

attache {
    transitive.set(true)
}

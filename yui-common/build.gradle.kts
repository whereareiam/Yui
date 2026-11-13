plugins {
    id("module-java")
}

dependencies {
    compileOnly(projects.yuiApi)

    testImplementation(projects.yuiApi)
    testImplementation(libs.spring.tx)
}

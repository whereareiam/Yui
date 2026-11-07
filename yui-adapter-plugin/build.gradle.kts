plugins {
    id("module-java")
}

dependencies {
    compileOnly(projects.yuiApi)

    testImplementation(projects.yuiApi)
}

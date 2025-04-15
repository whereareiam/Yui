dependencies {
    "implementation"(libs.bundles.spring)
    "implementation"(libs.bundles.configuration)
    "implementation"(libs.bundles.pf4j)
    "implementation"(libs.jda)
    "implementation"(libs.postgres)

    // include all projects
    rootProject.subprojects.forEach { subproject ->
        if (subproject.name != "yue-bootstrap") {
            "implementation"(project(":${subproject.name}"))
        }
    }
}
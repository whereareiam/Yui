dependencies {
    "implementation"(libs.bundles.spring)
    "implementation"(libs.bundles.configuration)

    // include all projects
    rootProject.subprojects.forEach { subproject ->
        if (subproject.name != "yue-bootstrap") {
            "implementation"(project(":${subproject.name}"))
        }
    }
}
dependencies {
    "implementation"(libs.bundles.spring)
    "implementation"(libs.bundles.configuration)
    "implementation"(libs.jda)
    "implementation"(libs.postgres)
    "implementation"(libs.emoji)

    // include all projects
    rootProject.subprojects.forEach { subproject ->
        if (subproject.name != "yue-bootstrap") {
            "implementation"(project(":${subproject.name}"))
        }
    }
}
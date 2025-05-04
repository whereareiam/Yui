dependencies {
    "implementation"(libs.bundles.spring)
    "implementation"(libs.bundles.configuration)
    "implementation"(libs.jda)
    "implementation"(libs.postgres)

    // include all projects
    rootProject.subprojects.forEach { subproject ->
        if (subproject.name != "yui-bootstrap") {
            "implementation"(project(":${subproject.name}"))
        }
    }
}
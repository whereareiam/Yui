import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

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

tasks.named<BootJar>("bootJar") {
    archiveFileName.set("Yui.jar")
    destinationDirectory.set(rootProject.buildDir.resolve("libs"))
}

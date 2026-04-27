import org.gradle.api.artifacts.VersionCatalogsExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("yui.java-common")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    add("implementation", libs.findLibrary("attache-spring").get())
    add("implementation", libs.findLibrary("attache-common").get())

    rootProject.subprojects
        .filter { it.path != ":yui-bootstrap" }
        .forEach { subproject ->
            add("implementation", project(subproject.path))
        }
}

tasks.named<BootJar>("bootJar").configure {
    mainClass.set("me.whereareiam.yui.YuiLauncher")
    archiveFileName.set("Yui.jar")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

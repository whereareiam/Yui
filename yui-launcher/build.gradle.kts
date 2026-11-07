import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("module-assembly")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

// Yui.jar holds Yui's modules and Attache; Attache loads every other library at startup.
dependencies {
    implementation(projects.yuiApi)
    implementation(projects.yuiCommon)
    implementation(projects.yuiAdapterCommand)
    implementation(projects.yuiAdapterDatabase)
    implementation(projects.yuiAdapterPlugin)

    implementation(libs.attache.spring)
    implementation(libs.attache.common)
}

tasks.named<BootJar>("bootJar") {
    mainClass.set("me.whereareiam.yui.launcher.YuiLauncher")
    archiveFileName.set("Yui.jar")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

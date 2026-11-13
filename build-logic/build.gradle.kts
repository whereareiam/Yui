plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://registry.whereareiam.me/maven/packages")
}

dependencies {
    implementation(libs.lombok.plugin)
    implementation(libs.toolkit.architecture)
    implementation(libs.toolkit.publish.maven)
}

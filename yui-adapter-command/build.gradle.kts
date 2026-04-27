plugins {
    id("yui.java-common")
}

dependencies {
    compileOnly(libs.bundles.cloud)
    testImplementation(libs.bundles.cloud)
}

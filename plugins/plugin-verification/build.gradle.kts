plugins {
    id("module-plugin")
}

description = "Verification for Yui: a new member picks their languages and gets the verified role."

dependencies {
    bundled(projects.plugins.pluginVerification.verificationApi)

    testImplementation(projects.yuiCommon)
    testImplementation(libs.spring.boot.data.jpa)
}

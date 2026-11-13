plugins {
    id("module-plugin")
}

description = "Onboarding for Yui: a new member picks their languages and gets the verified role."

dependencies {
    bundled(projects.plugins.pluginOnboarding.onboardingApi)

    testImplementation(projects.yuiCommon)
    testImplementation(libs.spring.boot.data.jpa)
}

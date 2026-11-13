plugins {
    id("module-plugin")
}

description = "Interests for Yui: members pick the topics they are here for, in onboarding and with /topics, and get their roles."

dependencies {
    compileOnly(projects.plugins.pluginOnboarding.onboardingApi)

    testImplementation(projects.yuiCommon)
    testImplementation(projects.plugins.pluginOnboarding.onboardingApi)
    testImplementation(libs.spring.boot.data.jpa)
}

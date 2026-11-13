plugins {
    id("module-plugin")
    id("me.whereareiam.yui.plugin")
}

description = "Interests for Yui: members pick the topics they are here for, in onboarding and with /topics, and get their roles."

dependencies {
    compileOnly(projects.plugins.pluginOnboarding.onboardingApi)

    testImplementation(projects.yuiCommon)
    testImplementation(projects.plugins.pluginOnboarding.onboardingApi)
    testImplementation(libs.spring.boot.data.jpa)
}

yuiPlugin {
    id.set("interests")
    main.set("me.whereareiam.yui.interests.Interests")
    dependencies.add("onboarding")
    updates.set("GITHUB:whereareiam/Yui")
}

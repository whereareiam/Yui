plugins {
    id("module-plugin")
    id("me.whereareiam.yui.plugin")
}

description = "Onboarding for Yui: a new member picks their languages and gets the verified role."

dependencies {
    bundled(projects.plugins.pluginOnboarding.onboardingApi)

    testImplementation(projects.yuiCommon)
    testImplementation(libs.spring.boot.data.jpa)
}

yuiPlugin {
    id.set("onboarding")
    main.set("me.whereareiam.yui.onboarding.Onboarding")
    exports.add("me.whereareiam.yui.onboarding.api")
    updates.set("GITHUB:whereareiam/Yui")
}

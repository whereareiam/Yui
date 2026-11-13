plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "me.whereareiam.yui"
version = providers.gradleProperty("yuiVersion").orElse("dev").get()
description = "Builds a plugin of Yui: writes yui-plugin.yml from the build and bundles the plugin's own modules into its jar."

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

gradlePlugin {
    plugins {
        create("yuiPlugin") {
            id = "me.whereareiam.yui.plugin"
            implementationClass = "me.whereareiam.yui.gradle.YuiPluginBuild"
            displayName = "Yui plugin"
            description = project.description
        }
    }
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

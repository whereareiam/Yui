plugins {
    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName("me.whereareiam.yui")

    buildConfigField("String", "NAME", "\"${rootProject.name}\"")
    buildConfigField("String", "VERSION", "\"${rootProject.version}\"")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "Yui"
            pom {
                name.set("Yui")
                description.set("Public API for Yui - Modular Discord Bot Framework")
            }
        }
    }
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        title = "Yui API"
        windowTitle = "Yui API"
    }
}
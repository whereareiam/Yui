plugins {
    id("maven-publish")
    alias(libs.plugins.buildconfig)
}

// libraries that should be transitively visible to API consumers
dependencies {
    "api"(rootProject.libs.jda)
    "api"(rootProject.libs.configura)
    "api"(rootProject.libs.semantica)
    "api"(rootProject.libs.annotations)
    "api"(rootProject.libs.spring.boot)
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

extensions.configure<PublishingExtension> {
    repositories {
        maven {
            val realm = (System.getenv("PUBLISH_REALM")
                ?: if ((System.getenv("VERSION") ?: "dev").contains("dev", true)) "development" else "release")
                .lowercase()
            url = uri("https://maven.whereareiam.me/$realm")
            credentials {
                username = System.getenv("PUBLISH_USER") ?: ""
                password = System.getenv("PUBLISH_TOKEN") ?: ""
            }
        }
    }
}
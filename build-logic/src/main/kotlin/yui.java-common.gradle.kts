import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

plugins {
    `java-library`
    `maven-publish`
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val buildVersion = providers.environmentVariable("VERSION").orElse("dev")

group = "me.whereareiam"
version = buildVersion.get()

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_25.toString()
    targetCompatibility = JavaVersion.VERSION_25.toString()
}

dependencies {
    add("compileOnly", libs.findLibrary("lombok").get())
    add("annotationProcessor", libs.findLibrary("lombok").get())
    add("testImplementation", libs.findLibrary("lombok").get())
    add("testAnnotationProcessor", libs.findLibrary("lombok").get())

    if (path != ":yui-api") {
        add("compileOnly", project(":yui-api"))
        add("testImplementation", project(":yui-api"))
    }

    if (path != ":yui-bootstrap") {
        add("compileOnly", libs.findLibrary("annotations").get())
        add("compileOnly", libs.findLibrary("spring-boot").get())
        add("compileOnly", libs.findLibrary("jda").get())
        add("compileOnly", libs.findLibrary("configura").get())
        add("compileOnly", libs.findLibrary("semantica").get())
        add("compileOnly", libs.findLibrary("attache-common").get())

        add("testImplementation", project(":yui-api"))
        add("testImplementation", libs.findLibrary("spring-boot-test").get())
        add("testImplementation", libs.findLibrary("jda").get())
        add("testImplementation", libs.findLibrary("configura").get())
        add("testImplementation", libs.findLibrary("semantica").get())
        add("testRuntimeOnly", libs.findLibrary("junit-platform-launcher").get())
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

extensions.configure<PublishingExtension> {
    repositories {
        maven {
            val realm = providers.environmentVariable("PUBLISH_REALM")
                .orElse(
                    buildVersion.map { versionString ->
                        if (versionString.contains("dev", ignoreCase = true)) "development" else "release"
                    }
                )
                .get()
                .lowercase()

            url = uri("https://maven.whereareiam.me/$realm")

            credentials {
                username = providers.environmentVariable("PUBLISH_USER").orNull.orEmpty()
                password = providers.environmentVariable("PUBLISH_TOKEN").orNull.orEmpty()
            }
        }
    }
}

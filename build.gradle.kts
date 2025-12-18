defaultTasks("build")

allprojects {
    version = (System.getenv("VERSION") ?: "dev")
    group = "me.whereareiam"

    apply(plugin = "java")
    apply(plugin = "maven-publish")

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_25.toString()
        targetCompatibility = JavaVersion.VERSION_25.toString()
        options.compilerArgs.add("--enable-preview")
    }
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://maven.whereareiam.me/release")
        maven("https://maven.whereareiam.me/development")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    if (project.name != "yui-api") {
        dependencies {
            "compileOnly"(project(":yui-api"))
        }
    }

    if (project.name != "yui-bootstrap") {
        dependencies {
            // lombok
            "compileOnly"(rootProject.libs.lombok)
            "annotationProcessor"(rootProject.libs.lombok)

            // general
            "compileOnly"(rootProject.libs.annotations)
            "compileOnly"(rootProject.libs.spring.boot)
            "compileOnly"(rootProject.libs.jda)
            "compileOnly"(rootProject.libs.configura)

            // testing
            "testImplementation"(project(":yui-api"))
            "testImplementation"(rootProject.libs.spring.boot.test)
            "testImplementation"(rootProject.libs.jda)
            "testImplementation"(rootProject.libs.configura)
            "testRuntimeOnly"(rootProject.libs.junit.platform.launcher)
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
}

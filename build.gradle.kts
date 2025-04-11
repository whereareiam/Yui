defaultTasks("build")

allprojects {
    version = (System.getenv("VERSION") ?: "dev")

    apply(plugin = "java")

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }
}

subprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "java")

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    if (project.name != "yue-common-api" && project.name != "yue-shared") {
        dependencies {
            "compileOnly"(project(":yue-common-api"))
            "compileOnly"(project(":yue-shared"))

            "compileOnly"(rootProject.libs.lombok)
            "annotationProcessor"(rootProject.libs.lombok)

            "testImplementation"(project(":yue-common-api"))
            "testImplementation"(rootProject.libs.spring.boot.test)
            "testImplementation"(rootProject.libs.jda)
        }
    }

    if (project.name != "yue-bootstrap") {
        dependencies {
            "compileOnly"(rootProject.libs.spring.boot)
            "compileOnly"(rootProject.libs.jda)
        }
    }
}

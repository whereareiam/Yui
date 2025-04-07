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

    if (project.name != "yue-common-api") {
        dependencies {
            "compileOnly"(project(":yue-common-api"))

            "compileOnly"(rootProject.libs.lombok)
            "annotationProcessor"(rootProject.libs.lombok)
        }
    }

    if (project.name != "yue-bootstrap") {
        dependencies {
            "compileOnly"(rootProject.libs.jda)
        }
    }
}
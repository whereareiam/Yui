defaultTasks("build")

allprojects {
    version = (System.getenv("VERSION") ?: "dev")

    apply(plugin = "java")

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_23.toString()
        targetCompatibility = JavaVersion.VERSION_23.toString()
        options.compilerArgs.add("--enable-preview")
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

    if (project.name != "yui-api" && project.name != "yui-shared") {
        dependencies {
            "compileOnly"(project(":yui-api"))
            "compileOnly"(project(":yui-shared"))

            "testImplementation"(project(":yui-api"))
            "testImplementation"(rootProject.libs.spring.boot.test)
            "testImplementation"(rootProject.libs.jda)
        }
    }

    if (project.name != "yui-bootstrap") {
        dependencies {
            "compileOnly"(rootProject.libs.spring.boot)
            "compileOnly"(rootProject.libs.jda)

            "compileOnly"(rootProject.libs.lombok)
            "annotationProcessor"(rootProject.libs.lombok)
        }
    }
}

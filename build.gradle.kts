defaultTasks("build")

allprojects {
    version = (System.getenv("VERSION") ?: "dev")
    group = "me.whereareiam"

    apply(plugin = "java-library")

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_25.toString()
        targetCompatibility = JavaVersion.VERSION_25.toString()
        options.compilerArgs.add("--enable-preview")
    }
}

subprojects {
    repositories {
        mavenLocal()
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
            "compileOnly"(rootProject.libs.semantica)
            "compileOnly"(rootProject.libs.attache.common)

            // testing
            "testImplementation"(project(":yui-api"))
            "testImplementation"(rootProject.libs.spring.boot.test)
            "testImplementation"(rootProject.libs.jda)
            "testImplementation"(rootProject.libs.configura)
            "testImplementation"(rootProject.libs.semantica)
            "testRuntimeOnly"(rootProject.libs.junit.platform.launcher)
        }
    }
}

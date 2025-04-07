defaultTasks("build")

allprojects {
    version = (System.getenv("VERSION") ?: "dev")
}

subprojects {
    repositories {
        mavenCentral()
    }
}
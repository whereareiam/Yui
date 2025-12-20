dependencies {
    "compileOnly"(libs.spring.boot.data.jpa)

    "testImplementation"(libs.postgres)
    "testImplementation"(libs.spring.boot.data.jpa)
    "testImplementation"(libs.testcontainers.junit)
    "testImplementation"(libs.testcontainers.postgres)
}
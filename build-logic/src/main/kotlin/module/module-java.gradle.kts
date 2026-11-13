import me.whereareiam.yui.buildlogic.module.catalog
import me.whereareiam.yui.buildlogic.module.library
import me.whereareiam.yui.buildlogic.module.version
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    id("io.freefair.lombok")
    id("me.whereareiam.toolkit.architecture")
}

// Compilation and test defaults of every Java module of Yui.
val javaVersion = catalog.version("java").toInt()

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion)
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

lombok {
    version.set(catalog.version("lombok"))
}

// The libraries Yui loads at runtime through Attache. Modules compile against them; tests get them on their
// classpath, since nothing loads them there.
val providedLibraries = listOf("spring-boot", "jda", "configura", "configura-polymorphic", "strata", "strata-configura", "semantica", "attache-common")

dependencies {
    compileOnly(catalog.library("annotations"))
    providedLibraries.forEach { compileOnly(catalog.library(it)) }

    testCompileOnly(catalog.library("annotations"))
    providedLibraries.forEach { testImplementation(catalog.library(it)) }
    testImplementation(catalog.library("spring-boot-test"))
    testRuntimeOnly(catalog.library("junit-platform"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("failed", "skipped")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

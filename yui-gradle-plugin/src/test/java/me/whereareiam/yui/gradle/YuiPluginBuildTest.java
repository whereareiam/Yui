package me.whereareiam.yui.gradle;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Builds a plugin with an -api of its own on a real Gradle build and reads its jar.
 */
class YuiPluginBuildTest {
	@TempDir
	Path project;

	@Test
	void theJar_HoldsTheDescriptorAndTheBundledModule() throws IOException {
		write("settings.gradle.kts", """
				rootProject.name = "greeting"
				include("greeting-api")
				""");
		write("build.gradle.kts", """
				plugins { id("me.whereareiam.yui.plugin") }
				version = "1.2.0"
				yuiPlugin {
				    id.set("greeting")
				    main.set("org.example.Greeting")
				    dependencies.add("onboarding")
				    exports.add("org.example.api")
				    updates.set("GITHUB:example/greeting")
				}
				dependencies { bundled(project(":greeting-api")) }
				""");
		write("src/main/java/org/example/Greeting.java", "package org.example; public class Greeting {}");
		write("greeting-api/build.gradle.kts", "plugins { `java-library` }");
		write("greeting-api/src/main/java/org/example/api/Greeter.java", "package org.example.api; public interface Greeter {}");

		GradleRunner.create().withProjectDir(project.toFile()).withPluginClasspath().withArguments("jar").build();

		try (ZipFile jar = new ZipFile(project.resolve("build/libs/greeting-1.2.0.jar").toFile())) {
			assertNotNull(jar.getEntry("org/example/api/Greeter.class"));
			assertEquals("""
					id: greeting
					version: "1.2.0"
					main: org.example.Greeting
					dependencies:
					  - id: onboarding
					exports:
					  - org.example.api
					updater:
					  release: { provider: GITHUB, id: example/greeting }
					""", new String(jar.getInputStream(jar.getEntry("yui-plugin.yml")).readAllBytes(), StandardCharsets.UTF_8));
		}
	}

	@Test
	void aVersionThatIsNoSemanticOne_IsWrittenAsAPreRelease() throws IOException {
		write("settings.gradle.kts", "rootProject.name = \"greeting\"");
		write("build.gradle.kts", """
				plugins { id("me.whereareiam.yui.plugin") }
				version = "dev"
				yuiPlugin {
				    id.set("greeting")
				    main.set("org.example.Greeting")
				}
				""");

		GradleRunner.create().withProjectDir(project.toFile()).withPluginClasspath().withArguments("yuiPluginDescriptor").build();

		String descriptor = Files.readString(project.resolve("build/generated/yui-plugin/yui-plugin.yml"));
		assertEquals("version: \"0.0.0-dev\"", descriptor.lines().skip(1).findFirst().orElseThrow());
	}

	private void write(String path, String content) throws IOException {
		Path file = project.resolve(path);
		Files.createDirectories(file.getParent());
		Files.writeString(file, content);
	}
}

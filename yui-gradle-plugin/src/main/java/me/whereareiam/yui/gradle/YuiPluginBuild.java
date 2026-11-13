package me.whereareiam.yui.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyScopeConfiguration;
import org.gradle.api.artifacts.ResolvableConfiguration;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import java.util.stream.Collectors;

/**
 * {@code me.whereareiam.yui.plugin}: builds a plugin of Yui as one jar. The jar holds the plugin's classes, the modules
 * it lists under {@code bundled} (its own -api), and {@code yui-plugin.yml}, written from {@code yuiPlugin { }} and the
 * project's version.
 *
 * <pre>{@code
 * plugins {
 *     id("me.whereareiam.yui.plugin")
 * }
 *
 * yuiPlugin {
 *     id.set("onboarding")
 *     main.set("me.whereareiam.yui.onboarding.Onboarding")
 *     exports.add("me.whereareiam.yui.onboarding.api")
 * }
 *
 * dependencies {
 *     compileOnly("me.whereareiam.yui:api:<version>")
 *     bundled(project(":onboarding-api"))
 * }
 * }</pre>
 */
public class YuiPluginBuild implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(JavaLibraryPlugin.class);
		YuiPluginExtension descriptor = project.getExtensions().create("yuiPlugin", YuiPluginExtension.class);

		TaskProvider<WriteDescriptor> write = project.getTasks().register("yuiPluginDescriptor", WriteDescriptor.class, task -> {
			task.getId().set(descriptor.getId());
			task.getVersion().set(project.provider(() -> project.getVersion().toString()));
			task.getMain().set(descriptor.getMain());
			task.getDependencies().set(descriptor.getDependencies());
			task.getExports().set(descriptor.getExports());
			task.getUpdates().set(descriptor.getUpdates());
			task.getOutput().set(project.getLayout().getBuildDirectory().dir("generated/yui-plugin"));
		});
		project.getExtensions().getByType(SourceSetContainer.class).named("main", main -> main.getResources().srcDir(write));

		bundle(project);
	}

	// Modules listed under bundled are compiled against and packed into the jar; their own dependencies are not.
	private static void bundle(Project project) {
		DependencyScopeConfiguration bundled = project.getConfigurations().dependencyScope("bundled").get();
		project.getConfigurations().named("implementation", implementation -> implementation.extendsFrom(bundled));
		ResolvableConfiguration classpath = project.getConfigurations().resolvable("bundledClasspath", resolvable -> {
			resolvable.extendsFrom(bundled);
			resolvable.setTransitive(false);
		}).get();

		project.getTasks().named("jar", Jar.class, jar -> {
			jar.dependsOn(classpath);
			jar.from(project.provider(() -> files(project, classpath)));
			jar.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
		});
	}

	private static Object files(Project project, Configuration classpath) {
		return classpath.getFiles().stream().map(project::zipTree).collect(Collectors.toList());
	}
}

package me.whereareiam.yui.adapter.plugin.bean;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.adapter.plugin.PluginStorage;
import me.whereareiam.yui.model.plugin.Dependency;
import me.whereareiam.yui.model.plugin.Plugin;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.*;

@Component
@AllArgsConstructor
public class DependencyBeanBridge {
	private final PluginStorage storage;

	public void bridge(AnnotationConfigApplicationContext childContext, Plugin plugin) {
		List<Dependency> deps = Optional.ofNullable(plugin.getDependencies()).orElse(List.of());
		if (deps.isEmpty()) return;

		ConfigurableListableBeanFactory childFactory = childContext.getBeanFactory();
		Set<Class<?>> registeredByType = new HashSet<>();

		for (Dependency d : deps) {
			if (!d.isInjectClassLoader()) continue;
			var maybe = storage.byId(d.getId());
			if (maybe.isEmpty()) continue;
			var dep = maybe.get();

			String depEntrypoint = dep.getPlugin().getEntrypoint();
			int dot = depEntrypoint.lastIndexOf('.') >= 0 ? depEntrypoint.lastIndexOf('.') : depEntrypoint.length();
			String apiPrefix = depEntrypoint.substring(0, dot) + ".api.";

			ConfigurableListableBeanFactory depFactory = dep.getContext().getBeanFactory();
			for (String beanName : depFactory.getBeanDefinitionNames()) {
				BeanDefinition def;
				try {
					def = depFactory.getBeanDefinition(beanName);
				} catch (Exception ignored) {
					continue;
				}

				if (def.getRole() != BeanDefinition.ROLE_APPLICATION) continue;

				Object bean;
				try {
					bean = dep.getContext().getBean(beanName);
				} catch (Exception ignored) {
					continue;
				}

				for (Class<?> apiInterface : publicApiInterfaces(bean.getClass(), apiPrefix)) {
					if (registeredByType.add(apiInterface)) {
						try {
							childFactory.registerResolvableDependency(apiInterface, bean);
						} catch (Exception ignored) {
						}
					}
				}
			}
		}
	}

	private static List<Class<?>> publicApiInterfaces(Class<?> beanClass, String apiPrefix) {
		List<Class<?>> result = new ArrayList<>();
		for (Class<?> itf : getAllInterfaces(beanClass)) {
			Package p = itf.getPackage();
			String pkg = p != null ? p.getName() : "";
			if (pkg.startsWith(apiPrefix) && Modifier.isPublic(itf.getModifiers()) && isPureApiInterface(itf, apiPrefix))
				result.add(itf);
		}

		return result;
	}

	private static boolean isPureApiInterface(Class<?> itf, String apiPrefix) {
		for (Class<?> parent : itf.getInterfaces()) {
			Package pkg = parent.getPackage();
			String name = pkg != null ? pkg.getName() : "";

			if (!name.startsWith(apiPrefix)) return false;
			if (!isPureApiInterface(parent, apiPrefix)) return false;
		}

		return true;
	}

	private static Set<Class<?>> getAllInterfaces(Class<?> type) {
		Set<Class<?>> interfaces = new LinkedHashSet<>();
		Deque<Class<?>> stack = new ArrayDeque<>();
		stack.push(type);

		while (!stack.isEmpty()) {
			Class<?> c = stack.pop();
			for (Class<?> i : c.getInterfaces())
				if (interfaces.add(i)) stack.push(i);

			Class<?> s = c.getSuperclass();
			if (s != null && s != Object.class)
				stack.push(s);
		}

		return interfaces;
	}
}



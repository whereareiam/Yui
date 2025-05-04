package me.whereareiam.yui.adapter.plugin.bean;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@Service
public class PluginBeanRegistry {
	private final CopyOnWriteArrayList<PluginBeanDefinition<?>> beans = new CopyOnWriteArrayList<>();

	public <T> void register(String beanName, Class<T> type, Supplier<T> supplier) {
		beans.add(new PluginBeanDefinition<>(beanName, type, supplier));
	}

	public void remove(String beanName) {
		beans.removeIf(def -> beanName.equals(def.beanName()));
	}

	public void apply(AnnotationConfigApplicationContext ctx) {
		beans.forEach(def -> {
			if (def.beanName() != null) {
				ctx.registerBean(def.beanName(), def.beanClass(), def.supplier());
			} else {
				ctx.registerBean(def.beanClass(), def.supplier());
			}
		});
	}
}

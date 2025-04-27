package me.whereareiam.yue.adapter.plugin.bean;

import java.util.function.Supplier;

public record PluginBeanDefinition<T>(
		String beanName,
		Class<T> beanClass,
		Supplier<T> supplier
) {}

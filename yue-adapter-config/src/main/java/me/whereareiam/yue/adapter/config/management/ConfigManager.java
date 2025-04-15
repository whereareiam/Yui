package me.whereareiam.yue.adapter.config.management;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import me.whereareiam.yue.api.output.config.ConfigurationManager;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class ConfigManager implements ConfigurationManager {
	private final ObjectMapper objectMapper;

	private final Map<Class<?>, DefaultConfig<?>> templates = new HashMap<>();

	@Autowired
	public ConfigManager(ObjectMapper objectMapper,
	                     List<DefaultConfig<?>> defaultConfigs) {
		this.objectMapper = objectMapper;

		addTemplates(defaultConfigs);
	}

	@Override
	public void addDeserializer(Class<?> clazz, Object deserializer) {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(clazz, (JsonDeserializer) deserializer);
		objectMapper.registerModule(module);
	}

	@Override
	public void addSerializer(Class<?> clazz, Object serializer) {
		SimpleModule module = new SimpleModule();
		module.addSerializer(clazz, (JsonSerializer) serializer);
		objectMapper.registerModule(module);
	}

	@Override
	public void addTemplate(Class<?> clazz, DefaultConfig<?> template) {
		templates.put(clazz, template);
	}

	@Override
	public <T> DefaultConfig<T> getTemplate(Class<T> clazz) {
		return (DefaultConfig<T>) templates.get(clazz);
	}

	private void addTemplates(List<DefaultConfig<?>> defaultConfigs) {
		for (DefaultConfig<?> defaultConfig : defaultConfigs) {
			Class<?> configClass = getConfigClassFromDefaultConfig(defaultConfig);
			templates.put(configClass, defaultConfig);
		}
	}

	@Override
	public Class<?> getConfigClassFromDefaultConfig(DefaultConfig<?> defaultConfig) {
		Type[] interfaces = defaultConfig.getClass().getGenericInterfaces();
		for (Type type : interfaces) {
			if (type instanceof ParameterizedType &&
					((ParameterizedType) type).getRawType().equals(DefaultConfig.class)) {
				Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
				if (typeArgument instanceof Class<?>)
					return (Class<?>) typeArgument;
			}
		}
		throw new IllegalArgumentException("Didn't find config class for " + defaultConfig.getClass());
	}
}
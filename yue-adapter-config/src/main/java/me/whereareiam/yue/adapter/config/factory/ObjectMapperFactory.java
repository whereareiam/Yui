package me.whereareiam.yue.adapter.config.factory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import me.whereareiam.yue.api.type.ConfigurationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapperFactory {
	private final ConfigurationType configurationType;

	private ObjectMapper objectMapper;

	@Autowired
	public ObjectMapperFactory(@Qualifier("configurationType") ConfigurationType configurationType) {
		this.configurationType = configurationType;
	}

	public ObjectMapper createObjectMapper() {
		if (objectMapper != null) return objectMapper;

		switch (configurationType) {
			case JSON -> objectMapper = new JsonMapper();
			case YAML -> {
				YAMLFactory yamlFactory = new YAMLFactory()
						.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
						.enable(YAMLGenerator.Feature.INDENT_ARRAYS)
						.enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
						.disable(YAMLGenerator.Feature.SPLIT_LINES);
				objectMapper = new YAMLMapper(yamlFactory);
			}

			default -> throw new IllegalArgumentException("Unsupported configuration type");
		}

		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return objectMapper;
	}
}
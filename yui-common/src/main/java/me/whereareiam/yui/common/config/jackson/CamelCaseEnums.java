package me.whereareiam.yui.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Lets every enum of a configuration file be written in camel case ({@code nothing}, {@code localBuilds}), a
 * plugin's too, beside its constant's own name. Text that is neither goes to the enum's usual reading, so an enum
 * with names of its own keeps them.
 */
final class CamelCaseEnums extends BeanDeserializerModifier {
	@Override
	public JsonDeserializer<?> modifyEnumDeserializer(
			DeserializationConfig config,
			JavaType type,
			BeanDescription description,
			JsonDeserializer<?> deserializer
	) {
		return new Reading(type.getRawClass(), deserializer);
	}

	private static final class Reading extends StdDeserializer<Object> {
		private final Class<?> type;
		private final JsonDeserializer<?> usual;

		private Reading(Class<?> type, JsonDeserializer<?> usual) {
			super(type);
			this.type = type;
			this.usual = usual;
		}

		@Override
		public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			if (parser.currentToken() != JsonToken.VALUE_STRING) return usual.deserialize(parser, context);

			String text = parser.getText().trim();
			for (Object constant : type.getEnumConstants()) {
				String name = ((Enum<?>) constant).name();
				if (name.equals(text) || name.equals(CamelCase.toConstant(text))) return constant;
			}

			return usual.deserialize(parser, context);
		}
	}
}

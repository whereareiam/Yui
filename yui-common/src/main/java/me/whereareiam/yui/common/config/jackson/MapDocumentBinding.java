package me.whereareiam.yui.common.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Builder;
import lombok.NonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Binds a file whose top level is a map, such as {@code languages.yml} (locale to language) or
 * {@code style/palette.yml} (name to colour). Keys starting with {@code _}, such as Strata's {@code _version}, are
 * left out.
 *
 * @param <D> the document
 * @param <K> its keys
 * @param <V> its values
 */
@Builder
final class MapDocumentBinding<D, K, V> {
	private final @NonNull Class<D> document;
	private final @NonNull Class<V> value;
	private final @NonNull Function<String, K> readKey;
	private final @NonNull Function<K, String> writeKey;
	private final @NonNull Function<Map<K, V>, D> create;
	private final @NonNull Function<D, Map<K, V>> entries;

	void register(SimpleModule module) {
		module.addDeserializer(document, new Reader());
		module.addSerializer(document, new Writer());
	}

	private final class Reader extends StdDeserializer<D> {
		private Reader() {
			super(document);
		}

		@Override
		public D deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			JsonNode tree = parser.readValueAsTree();
			Map<K, V> map = new LinkedHashMap<>();
			for (Iterator<Map.Entry<String, JsonNode>> fields = tree.fields(); fields.hasNext(); ) {
				Map.Entry<String, JsonNode> field = fields.next();
				if (field.getKey().startsWith("_")) continue;

				K key;
				try {
					key = readKey.apply(field.getKey());
				} catch (IllegalArgumentException exception) {
					return context.reportInputMismatch(document, exception.getMessage());
				}

				map.put(key, context.readTreeAsValue(field.getValue(), value));
			}

			return create.apply(map);
		}
	}

	private final class Writer extends StdSerializer<D> {
		private Writer() {
			super(document);
		}

		@Override
		public void serialize(D content, JsonGenerator generator, SerializerProvider provider) throws IOException {
			generator.writeStartObject();
			for (Map.Entry<K, V> entry : entries.apply(content).entrySet())
				provider.defaultSerializeField(writeKey.apply(entry.getKey()), entry.getValue(), generator);

			generator.writeEndObject();
		}
	}
}

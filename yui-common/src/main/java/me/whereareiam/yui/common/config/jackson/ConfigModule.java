package me.whereareiam.yui.common.config.jackson;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import me.whereareiam.yui.model.type.Duration;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Yui's value types as configuration files write them: a {@link Duration} as {@code 1h30m}, a {@link Color} as
 * {@code #B57E3F}, a {@link DiscordLocale} as its tag ({@code en-US}). A list may also be written as its one value.
 */
public final class ConfigModule extends SimpleModule {
	public ConfigModule() {
		super("yui-config");

		scalar(Duration.class, Duration::parse, Duration::toString);
		scalar(Color.class, Color::decode, ConfigModule::hex);
		scalar(DiscordLocale.class, DiscordLocale::from, DiscordLocale::getLocale);
	}

	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);

		JsonFormat.Value singleValue = JsonFormat.Value.empty().withFeature(JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		for (Class<?> type : List.of(Collection.class, List.class, ArrayList.class))
			context.configOverride(type).setFormat(singleValue);
	}

	private <T> void scalar(Class<T> type, Function<String, T> read, Function<T, String> write) {
		addDeserializer(type, new StdScalarDeserializer<>(type) {
			@Override
			public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
				String text = parser.getValueAsString();
				if (text == null || text.isBlank()) return null;

				return read.apply(text.trim());
			}
		});
		addSerializer(type, new StdScalarSerializer<>(type) {
			@Override
			public void serialize(T value, JsonGenerator generator, SerializerProvider provider) throws IOException {
				generator.writeString(write.apply(value));
			}
		});
	}

	private static String hex(Color color) {
		return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
	}
}

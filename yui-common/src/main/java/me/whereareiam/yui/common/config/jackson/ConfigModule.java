package me.whereareiam.yui.common.config.jackson;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.yui.api.model.config.audience.Audiences;
import me.whereareiam.yui.api.model.config.audit.AuditRoute;
import me.whereareiam.yui.api.model.config.languages.Language;
import me.whereareiam.yui.api.model.config.languages.Languages;
import me.whereareiam.yui.api.model.config.style.Palette;
import me.whereareiam.yui.api.model.config.style.StyleColor;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.api.type.ConversationType;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import me.whereareiam.yui.api.type.update.UpdateCheck;
import me.whereareiam.yui.api.type.update.UpdateWarning;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Yui's value types as configuration files write them: a {@link Duration} as {@code 1h30m}, a {@link Color} as
 * {@code #B57E3F}, a {@link DiscordLocale} as its tag ({@code en-US}), the updater's choices in camel case
 * ({@code localBuilds}). A list may also be written as its one value, and text may name environment variables
 * ({@code "${DISCORD_TOKEN}"}), which are read when the file is.
 */
public final class ConfigModule extends SimpleModule {
	public ConfigModule() {
		super("yui-config");

		scalar(Duration.class, Duration::parse, Duration::toString);
		scalar(Color.class, Color::decode, ConfigModule::hex);
		scalar(DiscordLocale.class, DiscordLocale::from, DiscordLocale::getLocale);
		scalar(String.class, EnvironmentText::resolve, Function.identity());
		camelCase(UpdateCheck.class);
		camelCase(UpdateWarning.class);
		scalar(StyleColor.class, StyleColor::parse, StyleColor::toString);
		camelCase(AuditCategory.class);
		camelCase(ConversationType.class);
		camelCaseKeys(AuditCategory.class);
		scalar(AuditRoute.class, ConfigModule::route, ConfigModule::route);

		MapDocumentBinding.<Languages, DiscordLocale, Language>builder()
				.document(Languages.class)
				.value(Language.class)
				.readKey(ConfigModule::locale)
				.writeKey(DiscordLocale::getLocale)
				.create(Languages::new)
				.entries(Languages::all)
				.build()
				.register(this);
		MapDocumentBinding.<Audiences, String, Requirements>builder()
				.document(Audiences.class)
				.value(Requirements.class)
				.readKey(ConfigModule::audienceName)
				.writeKey(Function.identity())
				.create(Audiences::new)
				.entries(Audiences::all)
				.build()
				.register(this);
		MapDocumentBinding.<Palette, String, Color>builder()
				.document(Palette.class)
				.value(Color.class)
				.readKey(ConfigModule::paletteName)
				.writeKey(Function.identity())
				.create(Palette::new)
				.entries(Palette::all)
				.build()
				.register(this);
	}

	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);
		context.addBeanDeserializerModifier(new CamelCaseEnums());

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

	private <E extends Enum<E>> void camelCase(Class<E> type) {
		scalar(type, text -> Enum.valueOf(type, CamelCase.toConstant(text)), value -> CamelCase.fromConstant(value.name()));
	}

	private <E extends Enum<E>> void camelCaseKeys(Class<E> type) {
		addKeyDeserializer(type, new KeyDeserializer() {
			@Override
			public Object deserializeKey(String key, DeserializationContext context) {
				return Enum.valueOf(type, CamelCase.toConstant(key));
			}
		});
		addKeySerializer(type, new StdKeySerializers.StringKeySerializer() {
			@Override
			public void serialize(Object value, JsonGenerator generator, SerializerProvider provider) throws IOException {
				generator.writeFieldName(CamelCase.fromConstant(((Enum<?>) value).name()));
			}
		});
	}

	// An entry of audit.yml goes off (YAML may read "off" as false) or to another category.
	private static AuditRoute route(String text) {
		if (text.equals("off") || text.equals("false")) return new AuditRoute.Off();

		return new AuditRoute.To(AuditCategory.valueOf(CamelCase.toConstant(text)));
	}

	private static String route(AuditRoute route) {
		return switch (route) {
			case AuditRoute.Off ignored -> "off";
			case AuditRoute.To to -> CamelCase.fromConstant(to.category().name());
		};
	}

	private static DiscordLocale locale(String tag) {
		DiscordLocale locale = DiscordLocale.from(tag);
		if (locale == DiscordLocale.UNKNOWN)
			throw new IllegalArgumentException("'" + tag + "' is no Discord locale, such as en-US or de");

		return locale;
	}

	private static String audienceName(String name) {
		if (!StyleColor.NAME.matcher(name).matches())
			throw new IllegalArgumentException("'" + name + "' is no audience name; write lowercase letters, digits and -");

		return name;
	}

	private static String paletteName(String name) {
		if (!StyleColor.NAME.matcher(name).matches())
			throw new IllegalArgumentException("'" + name + "' is no palette name; write lowercase letters, digits and -");

		return name;
	}

	private static String hex(Color color) {
		return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
	}
}

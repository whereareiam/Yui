package me.whereareiam.yui.adapter.command.registration.annotation;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.adapter.command.YuiCommandMetaKeys;
import me.whereareiam.yui.annotation.command.Definition;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.extractor.ArgumentExtractor;
import org.incendo.cloud.annotations.extractor.CommandExtractor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Wrapper around Cloud's AnnotationParser that recognizes Yui's custom annotations
 * ({@link me.whereareiam.yui.annotation.command.Command}, {@link me.whereareiam.yui.annotation.command.Argument})
 * instead of Cloud's annotations.
 * <p>
 * This allows Yui to use Cloud's annotation parser without exposing cloud-annotations
 * as a direct dependency to plugin authors.
 */
@RequiredArgsConstructor
public class YuiAnnotationParser<C> {
	private final AnnotationParser<C> cloudParser;
	private final CommandExtractor yuiCommandExtractor;
	private final ArgumentExtractor yuiArgumentExtractor;

	/**
	 * Creates a new Yui annotation parser wrapping Cloud's parser.
	 *
	 * @param commandManager the command manager to use
	 * @param senderType     the sender type class
	 * @param <C>            sender type
	 * @return new Yui annotation parser
	 */
	public static <C> YuiAnnotationParser<C> create(
			@NotNull CommandManager<C> commandManager,
			@NotNull Class<C> senderType
	) {
		AnnotationParser<C> cloudParser = new AnnotationParser<>(commandManager, senderType);
		
		// Create custom extractors that recognize Yui annotations
		YuiCommandExtractor yuiCommandExtractor = new YuiCommandExtractor(cloudParser);
		YuiArgumentExtractor yuiArgumentExtractor = new YuiArgumentExtractor(cloudParser);
		
		// Replace Cloud's extractors with Yui-aware ones
		cloudParser.commandExtractor(yuiCommandExtractor);
		cloudParser.argumentExtractor(yuiArgumentExtractor);
		
		YuiAnnotationParser<C> parser = new YuiAnnotationParser<>(
				cloudParser,
				yuiCommandExtractor,
				yuiArgumentExtractor
		);
		
		// Register builder modifier for @Definition annotation
		cloudParser.registerBuilderModifier(
				Definition.class,
				(annotation, builder) -> builder.meta(YuiCommandMetaKeys.DEFINITION, annotation.value())
		);
		
		return parser;
	}

	/**
	 * Returns the underlying Cloud annotation parser.
	 * <p>
	 * This allows access to Cloud's advanced features if needed.
	 *
	 * @return Cloud annotation parser
	 */
	public @NotNull AnnotationParser<C> cloudParser() {
		return cloudParser;
	}

	/**
	 * Parses command instances annotated with Yui annotations.
	 *
	 * @param instances command instances to parse
	 * @return collection of parsed commands
	 */
	public @NotNull Collection<@NotNull Command<C>> parse(@NotNull Object @NotNull... instances) {
		return cloudParser.parse(instances);
	}

	/**
	 * Parses command instances annotated with Yui annotations.
	 *
	 * @param instances command instances to parse
	 * @return collection of parsed commands
	 */
	public @NotNull Collection<@NotNull Command<C>> parse(@NotNull Collection<@NotNull Object> instances) {
		return cloudParser.parse(instances);
	}
}

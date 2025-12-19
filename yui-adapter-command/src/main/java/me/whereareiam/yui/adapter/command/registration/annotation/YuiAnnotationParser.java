package me.whereareiam.yui.adapter.command.registration.annotation;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.annotation.command.Definition;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
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
		AnnotationParser<C> cloudParser = new AnnotationParser<>(
				new RecordingCommandManager<>(commandManager),
				senderType
		);
		
		// Create custom extractors that recognize Yui annotations
		YuiCommandExtractor yuiCommandExtractor = new YuiCommandExtractor(cloudParser);
		YuiArgumentExtractor yuiArgumentExtractor = new YuiArgumentExtractor(cloudParser);
		
		// Replace Cloud's extractors with Yui-aware ones
		cloudParser.commandExtractor(yuiCommandExtractor);
		cloudParser.argumentExtractor(yuiArgumentExtractor);
		
		YuiAnnotationParser<C> parser = new YuiAnnotationParser<>(
				cloudParser
		);
		
		// Register builder modifier for @Definition annotation
		cloudParser.registerBuilderModifier(
				Definition.class,
				(annotation, builder) -> builder.meta(YuiCommandMetaKeys.DEFINITION, annotation.value())
		);
		
		return parser;
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
	 * Local recording manager used to let the annotation parser inspect commands
	 * without registering them directly on the real manager.
	 */
	private static final class RecordingCommandManager<C> extends CommandManager<C> {
		private final CommandManager<C> realManager;

		RecordingCommandManager(@NotNull CommandManager<C> realManager) {
			super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
			this.realManager = realManager;
		}

		@Override
		public boolean hasPermission(@NotNull C sender, @NotNull String permission) {
			return true;
		}

		/**
		 * Delegates to the real command manager's parser registry so that
		 * suggestions registered in the real manager are available during parsing.
		 */
		@Override
		public @NotNull org.incendo.cloud.parser.ParserRegistry<C> parserRegistry() {
			return realManager.parserRegistry();
		}
	}
}

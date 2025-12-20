package me.whereareiam.yui.adapter.command.parsing.annotation;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Default;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.ArgumentMode;
import org.incendo.cloud.annotations.SyntaxFragment;
import org.incendo.cloud.annotations.descriptor.ArgumentDescriptor;
import org.incendo.cloud.annotations.extractor.ArgumentExtractor;
import org.incendo.cloud.annotations.extractor.ParameterNameExtractor;
import org.incendo.cloud.component.DefaultValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom argument extractor that recognizes Yui's {@link me.whereareiam.yui.annotation.command.Argument}
 * annotation and adapts it to Cloud's argument descriptor format.
 * <p>
 * This implementation mirrors Cloud's StandardArgumentExtractor but works with Yui annotations.
 */
@RequiredArgsConstructor
class YuiArgumentExtractor implements ArgumentExtractor {
	private final AnnotationParser<?> annotationParser;
	private final ParameterNameExtractor parameterNameExtractor = ParameterNameExtractor.simple();

	@Override
	public @NotNull Collection<@NotNull ArgumentDescriptor> extractArguments(
			@NotNull List<@NotNull SyntaxFragment> syntax,
			@NotNull Method method
	) {
		final Map<String, SyntaxFragment> variableFragments = new HashMap<>();
		syntax.stream()
				.filter(fragment -> fragment.argumentMode() != ArgumentMode.LITERAL)
				.forEach(fragment -> variableFragments.put(fragment.major(), fragment));

		final Collection<ArgumentDescriptor> arguments = new ArrayList<>();
		for (final Parameter parameter : method.getParameters()) {
			final String parameterName = parameterNameExtractor.extract(parameter);

			// Handle @Default annotation (Yui's version - simpler, only has value())
			DefaultValue<?, ?> defaultValue = null;
			if (parameter.isAnnotationPresent(Default.class)) {
				final Default defaultAnnotation = parameter.getAnnotation(Default.class);
				defaultValue = DefaultValue.parsed(annotationParser.processString(defaultAnnotation.value()));
			}

			// Handle Yui's @Argument annotation
			if (!parameter.isAnnotationPresent(Argument.class)) {
				// No @Argument annotation - try to infer from parameter name
				final SyntaxFragment fragment = variableFragments.get(parameterName);
				if (fragment != null) {
					arguments.add(
							ArgumentDescriptor.builder()
									.parameter(parameter)
									.defaultValue(defaultValue)
									.name(parameterName)
									.build()
					);
				}
				continue;
			}

			final Argument yuiArgument = parameter.getAnnotation(Argument.class);
			// If value() is empty, infer from parameter name (mirrors Cloud's behaviour)
			final String rawName = yuiArgument.value().isEmpty()
					? parameterName
					: yuiArgument.value();
			final String name = annotationParser.processString(rawName);

			final String parserName = nullIfEmpty(annotationParser.processString(yuiArgument.parser()));
			final String suggestions = nullIfEmpty(annotationParser.processString(yuiArgument.suggestions()));
			final String description = nullIfEmpty(annotationParser.processString(yuiArgument.description()));

			final ArgumentDescriptor argumentDescriptor = ArgumentDescriptor.builder()
					.parameter(parameter)
					.name(name)
					.parserName(parserName)
					.defaultValue(defaultValue)
					.description(description != null ? annotationParser.mapDescription(description) : null)
					.suggestions(suggestions)
					.build();
			arguments.add(argumentDescriptor);
		}
		return arguments;
	}

	private static @Nullable String nullIfEmpty(@NotNull String string) {
		return string.isEmpty() ? null : string;
	}
}

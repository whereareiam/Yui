package me.whereareiam.yui.adapter.command.registration.annotation;

import me.whereareiam.yui.adapter.command.registration.CommandDefinitionParser;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.model.command.CommandDefinition;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Bridges Yui's annotation parsing {@link YuiAnnotationParser} with {@link CommandDefinition}
 * based registration via {@link CommandDefinitionParser}.
 *
 * @param <S> sender type
 */
public final class AnnotationCommandRegistrar<S> {
    private final CommandDefinitionParser<S> definitionParser;
    private final YuiAnnotationParser<S> annotationParser;
    private Function<String, CommandDefinition> definitionLookup;

    public AnnotationCommandRegistrar(
            @NotNull CommandDefinitionParser<S> definitionParser,
            @NotNull Class<S> senderType,
            @NotNull Function<String, CommandDefinition> definitionLookup
    ) {
        this.definitionParser = Objects.requireNonNull(definitionParser, "definitionParser");
        this.definitionLookup = Objects.requireNonNull(definitionLookup, "definitionLookup");

        this.annotationParser = YuiAnnotationParser.create(definitionParser.getCommandManager(), senderType);
    }

    /**
     * Updates the definition lookup used when resolving definitions during registration.
     */
    public void setDefinitionLookup(@NotNull Function<String, CommandDefinition> definitionLookup) {
        this.definitionLookup = Objects.requireNonNull(definitionLookup, "definitionLookup");
    }

    /**
     * Registers all commands present in the given containers, using Yui annotations and CommandDefinitions.
     */
    public void register(@Nullable String rootCommand, @NotNull Object... containers) {
        for (Command<S> parsed : this.annotationParser.parse(containers)) {
            String definitionId = definitionId(parsed);
            if (definitionId == null) {
                // No definition binding; register the parsed command as-is
                definitionParser.getCommandManager().command(parsed);
                continue;
            }

            CommandDefinition definition = definitionLookup.apply(definitionId);
            if (definition == null) {
                // Missing definition: register parsed command as-is
                definitionParser.getCommandManager().command(parsed);
                continue;
            }

            if (!definition.isEnabled()) {
                // Definition present but disabled: skip registration entirely
                continue;
            }

            Map<String, CommandComponent<S>> componentsByName = indexArgumentComponents(parsed);
            List<CommandDefinitionParser.ArgumentToken> tokens = definitionParser.parseArguments(definition.getUsage());

            // Decide whether this should be registered as subcommand based on usage
            String effectiveRoot = isSubcommand(rootCommand, definition) ? rootCommand : null;
            List<Command.Builder<S>> builders = definitionParser.buildFromDefinition(
                    definition,
                    definitionId,
                    tokens,
                    componentsByName,
                    parsed,
                    effectiveRoot
            );

            for (Command.Builder<S> builder : builders) {
                definitionParser.getCommandManager().command(builder);
            }
        }
    }

    private @Nullable String definitionId(Command<S> parsed) {
        return parsed.commandMeta()
                .optional(YuiCommandMetaKeys.DEFINITION)
                .orElse(null);
    }

    private boolean isSubcommand(@Nullable String rootCommand, @NotNull CommandDefinition definition) {
        String usage = definition.getUsage();
        return rootCommand != null && usage != null && usage.contains("{command}");
    }

    private Map<String, CommandComponent<S>> indexArgumentComponents(Command<S> parsedCommand) {
        Map<String, CommandComponent<S>> map = new HashMap<>();

        for (CommandComponent<S> component : parsedCommand.components()) {
            if (component.type() == CommandComponent.ComponentType.LITERAL) continue;
            map.put(component.name(), component);
        }

        return map;
    }
}
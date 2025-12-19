package me.whereareiam.yui.adapter.command.registration;

import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.adapter.command.parsing.annotation.YuiAnnotationParser;
import me.whereareiam.yui.adapter.command.parsing.definition.CommandDefinitionParser;
import me.whereareiam.yui.model.command.CommandDefinition;
import org.incendo.cloud.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
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

            // Decide whether this should be registered as subcommand based on usage
            String effectiveRoot = isSubcommand(rootCommand, definition) ? rootCommand : null;
            
            // Apply CommandDefinition overrides to the parsed command
            List<Command.Builder<S>> builders = definitionParser.applyOverrides(
                    definition,
                    definitionId,
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
}
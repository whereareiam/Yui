package me.whereareiam.yui.adapter.command.registration.annotation;

import me.whereareiam.yui.adapter.command.registration.CommandDefinitionParser;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.model.command.CommandDefinition;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.parser.ParserRegistry;
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
    private final Function<String, CommandDefinition> definitionLookup;

    public AnnotationCommandRegistrar(
            @NotNull CommandDefinitionParser<S> definitionParser,
            @NotNull Class<S> senderType,
            @NotNull Function<String, CommandDefinition> definitionLookup
    ) {
        this.definitionParser = Objects.requireNonNull(definitionParser, "definitionParser");
        this.definitionLookup = Objects.requireNonNull(definitionLookup, "definitionLookup");

        CommandManager<S> realManager = definitionParser.getCommandManager();
        RecordingCommandManager<S> recordingManager = new RecordingCommandManager<>(realManager);
        this.annotationParser = YuiAnnotationParser.create(recordingManager, senderType);
    }

    /**
     * Registers all commands present in the given containers, using Yui annotations and CommandDefinitions.
     */
    public void register(@NotNull Object... containers) {
	    String root = definitionParser.getRootCommand();

        for (Command<S> parsed : this.annotationParser.parse(containers)) {
            String definitionId = definitionId(parsed);
            if (definitionId == null) continue;

            CommandDefinition definition = definitionLookup.apply(definitionId);
            if (definition == null || !definition.isEnabled()) continue;

            Map<String, CommandComponent<S>> componentsByName = indexArgumentComponents(parsed);
            List<CommandDefinitionParser.ArgumentToken> tokens = definitionParser.parseArguments(definition.getUsage());

            // Decide whether this should be registered as subcommand based on usage
            if (isSubcommand(root, definition))
                definitionParser.setRootCommand(root);

            definitionParser.registerFromDefinition(definition, definitionId, tokens, componentsByName, parsed);
        }
    }

    /**
     * Sets a logical root command for subcommand-style usages (when usage contains {@code {command}}).
     */
    public void setRootCommand(@NotNull String root) {
        this.definitionParser.setRootCommand(root);
    }

    /**
     * Resolves the {@link CommandDefinition} associated with the given command, if any.
     */
    public @NotNull Optional<CommandDefinition> resolveDefinition(@NotNull Command<S> command) {
        return command.commandMeta()
                .optional(YuiCommandMetaKeys.DEFINITION)
                .map(definitionLookup);
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

    /**
     * Local recording manager used to let the annotation parser inspect commands
     * without registering them directly on the real manager.
     */
    private static final class RecordingCommandManager<S> extends CommandManager<S> {
        private final CommandManager<S> realManager;

        RecordingCommandManager(@NotNull CommandManager<S> realManager) {
            super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
            this.realManager = realManager;
        }

        @Override
        public boolean hasPermission(@NotNull S sender, @NotNull String permission) {
            return true;
        }

        /**
         * Delegates to the real command manager's parser registry so that
         * suggestions registered in the real manager are available during parsing.
         */
        @Override
        public @NotNull ParserRegistry<S> parserRegistry() {
            return realManager.parserRegistry();
        }
    }
}
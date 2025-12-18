package me.whereareiam.yui.adapter.command.registration;

import io.leangen.geantyref.TypeToken;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.adapter.command.registration.annotation.YuiAnnotationParser;
import me.whereareiam.yui.model.command.CommandDefinition;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Builds and registers Cloud commands from Yui {@link CommandDefinition} instances.
 * <p>
 * This is the final step that decides how commands are exposed (aliases, usage, description),
 * based on the configuration model, using argument components parsed by {@link YuiAnnotationParser}.
 *
 * @param <S> sender type
 */
public final class CommandDefinitionParser<S> {
    private final CommandManager<S> commandManager;
    private @Nullable String rootCommand;

    public CommandDefinitionParser(@NotNull CommandManager<S> commandManager) {
        this.commandManager = Objects.requireNonNull(commandManager, "commandManager");
    }

    public @NotNull CommandManager<S> getCommandManager() {
        return commandManager;
    }

    public void setRootCommand(@Nullable String rootCommand) {
        this.rootCommand = rootCommand;
    }

    public @Nullable String getRootCommand() {
        return rootCommand;
    }

    /**
     * Registers commands for the given {@link CommandDefinition}, using the provided parsed command
     * and its argument components.
     */
    public void registerFromDefinition(
            @NotNull CommandDefinition definition,
            @NotNull String definitionId,
            @NotNull List<ArgumentToken> argumentTokens,
            @NotNull Map<String, CommandComponent<S>> componentsByName,
            @NotNull Command<S> parsedCommand
    ) {
        if (!definition.isEnabled()) {
            return;
        }

        List<String> aliases = requireAliases(definition);
        AliasGroups groups = splitAliases(aliases);

        registerSingleWordAliases(definition, definitionId, argumentTokens, componentsByName, parsedCommand, groups.singleWord());
        registerMultiWordAliases(definition, definitionId, argumentTokens, componentsByName, parsedCommand, groups.multiWord());
    }

    private void registerSingleWordAliases(
            CommandDefinition definition,
            String definitionId,
            List<ArgumentToken> argumentTokens,
            Map<String, CommandComponent<S>> componentsByName,
            Command<S> parsedCommand,
            List<String> singleWordAliases
    ) {
        if (singleWordAliases.isEmpty()) return;

        String primary = singleWordAliases.getFirst();
        String[] secondary = singleWordAliases.stream().skip(1).toArray(String[]::new);

        Command.Builder<S> builder = (rootCommand != null)
                ? commandManager.commandBuilder(rootCommand).literal(primary, secondary)
                : commandManager.commandBuilder(primary, secondary);

        registerBuilt(definition, definitionId, argumentTokens, componentsByName, parsedCommand, builder);
    }

    private void registerMultiWordAliases(
            CommandDefinition definition,
            String definitionId,
            List<ArgumentToken> argumentTokens,
            Map<String, CommandComponent<S>> componentsByName,
            Command<S> parsedCommand,
            List<String> multiWordAliases
    ) {
        if (multiWordAliases.isEmpty()) return;

        Map<List<String>, List<String>> grouped = groupByPrefix(multiWordAliases);

        for (Map.Entry<List<String>, List<String>> entry : grouped.entrySet()) {
            List<String> prefix = entry.getKey();
            List<String> suffixes = distinct(entry.getValue());
            if (prefix.isEmpty() || suffixes.isEmpty()) continue;

            Command.Builder<S> builder = baseBuilder(prefix);
            builder = builder.literal(suffixes.getFirst(), suffixes.stream().skip(1).toArray(String[]::new));

            registerBuilt(definition, definitionId, argumentTokens, componentsByName, parsedCommand, builder);
        }
    }

    private Command.Builder<S> baseBuilder(List<String> prefixParts) {
        if (rootCommand != null) {
            Command.Builder<S> b = commandManager.commandBuilder(rootCommand);
            for (String p : prefixParts) {
                b = b.literal(p);
            }
            return b;
        }

        Command.Builder<S> b = commandManager.commandBuilder(prefixParts.getFirst());
        for (int i = 1; i < prefixParts.size(); i++) {
            b = b.literal(prefixParts.get(i));
        }
        return b;
    }

    private void registerBuilt(
            CommandDefinition definition,
            String definitionId,
            List<ArgumentToken> argumentTokens,
            Map<String, CommandComponent<S>> componentsByName,
            Command<S> parsedCommand,
            Command.Builder<S> builder
    ) {
        builder = addArguments(builder, argumentTokens, componentsByName);
        builder = applyMetadata(builder, definitionId, definition);
        builder = builder.handler(ctx -> parsedCommand.commandExecutionHandler().executeFuture(ctx));
        commandManager.command(builder);
    }

    private Command.Builder<S> addArguments(
            Command.Builder<S> builder,
            List<ArgumentToken> argumentTokens,
            Map<String, CommandComponent<S>> componentsByName
    ) {
        for (ArgumentToken token : argumentTokens) {
            CommandComponent<S> component = componentsByName.get(token.name());
            if (component == null) continue;
            builder = addComponent(builder, component, token.required());
        }
        return builder;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Command.Builder<S> addComponent(
            Command.Builder<S> builder,
            CommandComponent<S> component,
            boolean required
    ) {
        CommandComponent.Builder<?, ?> componentBuilder = createComponentBuilder(component);
        if (required) return builder.required((CommandComponent.Builder) componentBuilder);

        return builder.optional((CommandComponent.Builder) componentBuilder);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private CommandComponent.Builder<?, ?> createComponentBuilder(CommandComponent<S> component) {
        ArgumentParser rawParser = component.parser();
        TypeToken rawValueType = component.valueType();
        ParserDescriptor parserDescriptor = ParserDescriptor.of(rawParser, rawValueType);

        // We intentionally do not propagate the existing default value here to avoid
        // complex generic interactions between the source component and the new builder.
        // Default values can instead be provided by the CommandDefinition usage model
        // (e.g. via a future extension) or by the underlying parser.
        return CommandComponent.builder()
                .name(component.name())
                .parser(parserDescriptor)
                .description(component.description())
                .suggestionProvider(component.suggestionProvider());
    }

    private Command.Builder<S> applyMetadata(
            Command.Builder<S> builder,
            String definitionId,
            CommandDefinition definition
    ) {
        builder = builder.meta(YuiCommandMetaKeys.DEFINITION, definitionId);

        String description = definition.getDescription();
        if (description != null && !description.isBlank())
            builder = builder.commandDescription(Description.of(description));

        return builder;
    }

    private List<String> requireAliases(CommandDefinition definition) {
        List<String> aliases = sanitizeAliases(definition.getAliases());
        if (!aliases.isEmpty()) return aliases;
        throw new IllegalArgumentException("Command must define at least one alias");
    }

    private List<String> sanitizeAliases(@Nullable List<String> aliases) {
        if (aliases == null || aliases.isEmpty()) return Collections.emptyList();

        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String alias : aliases) {
            if (alias == null) continue;
            String trimmed = alias.trim();
            if (trimmed.isEmpty()) continue;
            out.add(trimmed);
        }

        return new ArrayList<>(out);
    }

    private AliasGroups splitAliases(List<String> aliases) {
        List<String> singleWord = new ArrayList<>();
        List<String> multiWord = new ArrayList<>();

        for (String alias : aliases) {
            if (alias.contains(" ")) {
                multiWord.add(alias);
                continue;
            }

            singleWord.add(alias);
        }
        return new AliasGroups(singleWord, multiWord);
    }

    /**
     * Parses the {@link CommandDefinition#getUsage()} string into argument tokens.
     */
    public List<ArgumentToken> parseArguments(@Nullable String usage) {
        if (usage == null || usage.isBlank()) return Collections.emptyList();

        List<ArgumentToken> tokens = new ArrayList<>();

        for (String token : usage.split("\\s+")) {
            if (token.isBlank()) continue;
            if (token.startsWith("{") && token.endsWith("}")) continue;

            boolean required = token.startsWith("<") && token.endsWith(">");
            boolean optional = token.startsWith("[") && token.endsWith("]");
            if (!required && !optional) continue;

            String cleaned = token.substring(1, token.length() - 1).trim();

            boolean greedy = cleaned.endsWith("...");
            if (greedy) cleaned = cleaned.substring(0, cleaned.length() - 3);

            if (cleaned.isEmpty()) continue;
            tokens.add(new ArgumentToken(cleaned, required, greedy));
        }

        return tokens;
    }

    private Map<List<String>, List<String>> groupByPrefix(List<String> multiWordAliases) {
        Map<List<String>, List<String>> grouped = new LinkedHashMap<>();

        for (String alias : multiWordAliases) {
            List<String> parts = splitAlias(alias);
            if (parts.size() < 2) continue;

            List<String> prefix = List.copyOf(parts.subList(0, parts.size() - 1));
            String suffix = parts.getLast();
            if (suffix.isBlank()) continue;

            grouped.computeIfAbsent(prefix, _ -> new ArrayList<>()).add(suffix);
        }

        return grouped;
    }

    private List<String> splitAlias(String alias) {
        return List.of(alias.trim().split("\\s+"));
    }

    private List<String> distinct(List<String> in) {
        if (in.isEmpty()) return in;
        return new ArrayList<>(new LinkedHashSet<>(in));
    }

    public record ArgumentToken(String name, boolean required, boolean greedy) {}

    private record AliasGroups(List<String> singleWord, List<String> multiWord) {}
}
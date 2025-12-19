package me.whereareiam.yui.adapter.command.registration;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.model.command.CommandDefinition;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.leangen.geantyref.TypeToken;
import java.util.*;

/**
 * Applies overrides from {@link CommandDefinition} to Cloud commands.
 * <p>
 * Takes a parsed command from {@link me.whereareiam.yui.adapter.command.registration.annotation.YuiAnnotationParser}
 * and applies CommandDefinition overrides (aliases, descriptions, usage) to create the final command builders.
 *
 * @param <S> sender type
 */
@RequiredArgsConstructor
public final class CommandDefinitionParser<S> {
    private final CommandManager<S> commandManager;

    public @NotNull CommandManager<S> getCommandManager() {
        return commandManager;
    }

    /**
     * Applies CommandDefinition overrides to the parsed command and returns command builders
     * for all aliases defined in the definition.
     *
     * @param definition     the command definition with overrides
     * @param definitionId   the ID of the definition
     * @param parsedCommand  the command parsed from annotations
     * @param rootCommand    optional root command (e.g., "yui" for "/yui reload")
     * @return list of command builders with definition overrides applied
     */
    public List<Command.Builder<S>> applyOverrides(
            @NotNull CommandDefinition definition,
            @NotNull String definitionId,
            @NotNull Command<S> parsedCommand,
            @Nullable String rootCommand
    ) {
        if (!definition.isEnabled())
            return List.of();

        List<String> aliases = sanitizeAliases(definition.getAliases());
        if (aliases.isEmpty())
            throw new IllegalArgumentException("Command must define at least one alias");

        // Extract components and usage from parsed command
        Map<String, CommandComponent<S>> variableComponents = extractVariableComponents(parsedCommand);
        List<VariableToken> usageTokens = parseUsage(definition.getUsage());

        // Group aliases by structure (single-word vs multi-word)
        AliasStructure structure = analyzeAliases(aliases);

        // Build commands for each alias group
        List<Command.Builder<S>> builders = new ArrayList<>();
        builders.addAll(buildCommandsForAliases(
                definition, definitionId, parsedCommand, variableComponents, usageTokens,
                structure.singleWord(), rootCommand
        ));
        builders.addAll(buildCommandsForAliases(
                definition, definitionId, parsedCommand, variableComponents, usageTokens,
                structure.multiWord(), rootCommand
        ));

        return builders;
    }

    /**
     * Builds command builders for a list of aliases.
     * For single-word aliases, creates one command per alias.
     * For multi-word aliases, groups them by prefix to create subcommands.
     */
    private List<Command.Builder<S>> buildCommandsForAliases(
            CommandDefinition definition,
            String definitionId,
            Command<S> parsedCommand,
            Map<String, CommandComponent<S>> variableComponents,
            List<VariableToken> usageTokens,
            List<String> aliases,
            @Nullable String rootCommand
    ) {
        if (aliases.isEmpty()) return List.of();

        List<Command.Builder<S>> builders = new ArrayList<>();

        // For single-word aliases, create one command per alias
        if (aliases.stream().noneMatch(alias -> alias.contains(" "))) {
            for (String alias : aliases) {
                Command.Builder<S> builder = createCommandBuilder(alias, definition, rootCommand);
                builder = applyVariables(builder, usageTokens, variableComponents, definition);
                builder = applyOverrides(builder, definition, definitionId);
                builder = builder.handler(ctx -> parsedCommand.commandExecutionHandler().executeFuture(ctx));
                builders.add(builder);
            }
            return builders;
        }

        // Multi-word aliases: group by prefix
        Map<List<String>, List<String>> grouped = groupByPrefix(aliases);
        for (Map.Entry<List<String>, List<String>> entry : grouped.entrySet()) {
            List<String> prefix = entry.getKey();
            List<String> suffixes = distinct(entry.getValue());
            if (prefix.isEmpty() || suffixes.isEmpty()) continue;

            Command.Builder<S> builder = buildPrefixPath(prefix, definition, rootCommand);
            Description suffixDescription = getComponentDescription(definition);
            builder = builder.literal(
                    suffixes.getFirst(),
                    suffixDescription,
                    suffixes.stream().skip(1).toArray(String[]::new)
            );

            builder = applyVariables(builder, usageTokens, variableComponents, definition);
            builder = applyOverrides(builder, definition, definitionId);
            builder = builder.handler(ctx -> parsedCommand.commandExecutionHandler().executeFuture(ctx));
            builders.add(builder);
        }

        return builders;
    }

    /**
     * Builds the prefix path for multi-word aliases.
     */
    private Command.Builder<S> buildPrefixPath(
            List<String> prefixParts,
            CommandDefinition definition,
            @Nullable String rootCommand
    ) {
        Description componentDescription = getComponentDescription(definition);

        if (rootCommand != null) {
            Command.Builder<S> builder = commandManager.commandBuilder(rootCommand);
            if (prefixParts.isEmpty()) return builder;
            
            builder = builder.literal(prefixParts.getFirst(), componentDescription);
            for (int i = 1; i < prefixParts.size(); i++)
                builder = builder.literal(prefixParts.get(i));
            return builder;
        }

        if (prefixParts.isEmpty())
            throw new IllegalArgumentException("Prefix parts cannot be empty");

        Command.Builder<S> builder = commandManager.commandBuilder(prefixParts.getFirst(), componentDescription);
        for (int i = 1; i < prefixParts.size(); i++)
            builder = builder.literal(prefixParts.get(i));

        return builder;
    }

    /**
     * Groups multi-word aliases by their prefix (all parts except the last).
     */
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

    /**
     * Removes duplicates from a list while preserving order.
     */
    private List<String> distinct(List<String> in) {
        if (in.isEmpty()) return in;
        return new ArrayList<>(new LinkedHashSet<>(in));
    }

    /**
     * Creates a command builder for the given alias with component description set.
     */
    private Command.Builder<S> createCommandBuilder(
            String alias,
            CommandDefinition definition,
            @Nullable String rootCommand
    ) {
        List<String> aliasParts = splitAlias(alias);
        Description componentDescription = getComponentDescription(definition);

        if (rootCommand != null) {
            Command.Builder<S> builder = commandManager.commandBuilder(rootCommand);
            if (aliasParts.isEmpty()) return builder;
            
            builder = builder.literal(aliasParts.getFirst(), componentDescription);
            for (int i = 1; i < aliasParts.size(); i++)
                builder = builder.literal(aliasParts.get(i));

            return builder;
        }

        if (aliasParts.isEmpty())
            throw new IllegalArgumentException("Alias cannot be empty");

        return commandManager.commandBuilder(
                aliasParts.getFirst(),
                componentDescription,
                aliasParts.stream().skip(1).toArray(String[]::new)
        );
    }

    /**
     * Applies variable components based on usage tokens.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Command.Builder<S> applyVariables(
            Command.Builder<S> builder,
            List<VariableToken> usageTokens,
            Map<String, CommandComponent<S>> variableComponents,
            CommandDefinition definition
    ) {
        for (VariableToken token : usageTokens) {
            CommandComponent<S> component = variableComponents.get(token.name());
            if (component == null) continue;

            CommandComponent.Builder<?, ?> componentBuilder = createComponentBuilder(component, definition);
            if (token.required()) {
                builder = builder.required((CommandComponent.Builder) componentBuilder);
                continue;
            }

            builder = builder.optional((CommandComponent.Builder) componentBuilder);
        }
        return builder;
    }

    /**
     * Applies CommandDefinition overrides (metadata, descriptions) to the builder.
     */
    private Command.Builder<S> applyOverrides(
            Command.Builder<S> builder,
            CommandDefinition definition,
            String definitionId
    ) {
        builder = builder.meta(YuiCommandMetaKeys.DEFINITION, definitionId);

        String description = definition.getDescription();
        if (description != null)
            builder = builder.commandDescription(Description.of(description));

        return builder;
    }

    /**
     * Creates a component builder from an existing component with variable description applied.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private CommandComponent.Builder<?, ?> createComponentBuilder(
            CommandComponent<S> component,
            CommandDefinition definition
    ) {
        ArgumentParser rawParser = component.parser();
        TypeToken rawValueType = component.valueType();
        ParserDescriptor parserDescriptor = ParserDescriptor.of(rawParser, rawValueType);

        Description variableDescription = getVariableDescription(component.name(), definition);

        return CommandComponent.builder()
                .name(component.name())
                .parser(parserDescriptor)
                .description(variableDescription)
                .suggestionProvider(component.suggestionProvider());
    }

    /**
     * Gets the variable description from CommandDefinition.
     * If not specified in the variables map, uses the variable name as description.
     */
    private Description getVariableDescription(String variableName, CommandDefinition definition) {
        Map<String, String> variables = definition.getVariables();
        if (variables != null && variables.containsKey(variableName)) {
            String description = variables.get(variableName);
            if (description != null && !description.isBlank())
                return Description.of(description);
        }

        // Fallback to variable name if not specified in map
        return Description.of(variableName);
    }

    /**
     * Extracts variable components (non-literals) from a parsed command.
     */
    private Map<String, CommandComponent<S>> extractVariableComponents(Command<S> parsedCommand) {
        Map<String, CommandComponent<S>> map = new HashMap<>();
        for (CommandComponent<S> component : parsedCommand.components())
            if (component.type() != CommandComponent.ComponentType.LITERAL)
                map.put(component.name(), component);

        return map;
    }

    /**
     * Parses the usage string into variable tokens.
     * <p>
     * This is public for testing purposes.
     */
    public List<VariableToken> parseUsage(@Nullable String usage) {
        if (usage == null || usage.isBlank()) return Collections.emptyList();

        List<VariableToken> tokens = new ArrayList<>();
        for (String token : usage.split("\\s+")) {
            if (token.isBlank() || (token.startsWith("{") && token.endsWith("}"))) continue;

            boolean required = token.startsWith("<") && token.endsWith(">");
            boolean optional = token.startsWith("[") && token.endsWith("]");
            if (!required && !optional) continue;

            String cleaned = token.substring(1, token.length() - 1).trim();
            boolean greedy = cleaned.endsWith("...");
            if (greedy) cleaned = cleaned.substring(0, cleaned.length() - 3);
            if (cleaned.isEmpty()) continue;

            tokens.add(new VariableToken(cleaned, required, greedy));
        }

        return tokens;
    }

    /**
     * Gets the component description from CommandDefinition.
     */
    private Description getComponentDescription(CommandDefinition definition) {
        String description = definition.getDescription();
        if (description != null && !description.isBlank())
            return Description.of(description);

        return Description.empty();
    }

    /**
     * Sanitizes and validates aliases from the definition.
     */
    private List<String> sanitizeAliases(@Nullable List<String> aliases) {
        if (aliases == null || aliases.isEmpty()) return Collections.emptyList();

        LinkedHashSet<String> sanitized = new LinkedHashSet<>();
        for (String alias : aliases) {
            if (alias == null) continue;
            String trimmed = alias.trim();
            if (trimmed.isEmpty()) continue;
            sanitized.add(trimmed);
        }

        return new ArrayList<>(sanitized);
    }

    /**
     * Analyzes aliases and groups them by structure (single-word vs multi-word).
     */
    private AliasStructure analyzeAliases(List<String> aliases) {
        List<String> singleWord = new ArrayList<>();
        List<String> multiWord = new ArrayList<>();

        for (String alias : aliases)
            (alias.contains(" ") ? multiWord : singleWord).add(alias);

        return new AliasStructure(singleWord, multiWord);
    }

    /**
     * Splits an alias string into parts.
     */
    private List<String> splitAlias(String alias) {
        return List.of(alias.trim().split("\\s+"));
    }

    /**
     * Represents a variable token parsed from usage string.
     */
    public record VariableToken(String name, boolean required, boolean greedy) {}

    /**
     * Groups aliases by their structure.
     */
    private record AliasStructure(List<String> singleWord, List<String> multiWord) {}
}

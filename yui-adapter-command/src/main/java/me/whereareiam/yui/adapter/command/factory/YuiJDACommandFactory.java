package me.whereareiam.yui.adapter.command.factory;

import io.leangen.geantyref.TypeToken;
import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.adapter.command.parser.FluctlightParser;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.message.Translatable;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.discord.jda6.JDACommandFactory;
import org.incendo.cloud.discord.jda6.JDAOptionType;
import org.incendo.cloud.discord.jda6.JDAParser;
import org.incendo.cloud.discord.slash.*;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom JDA command factory that adds multilanguage support for Discord commands.
 * <p>
 * Wraps Cloud's standard implementation and adds localization support from CommandDefinition
 * from the texts its descriptions name.
 */
public final class YuiJDACommandFactory implements JDACommandFactory<Interaction> {
    private final CommandTree<Interaction> commandTree;
    private final CommandDefinitionRegistry definitionRegistry;
    private final DiscordCommandFactory<Interaction> discordCommandFactory;
    private final NodeProcessor<Interaction> nodeProcessor;

    private CommandScopePredicate<Interaction> commandScopePredicate = CommandScopePredicate.alwaysTrue();

    public YuiJDACommandFactory(
            @NotNull CommandTree<Interaction> commandTree,
            @NotNull CommandDefinitionRegistry definitionRegistry,
            @NotNull FluctlightParser fluctlightParser
    ) {
        this.commandTree = Objects.requireNonNull(commandTree, "commandTree");
        this.definitionRegistry = Objects.requireNonNull(definitionRegistry, "definitionRegistry");

        // Reuse Cloud's standard option registry setup
        OptionRegistry<Interaction> optionRegistry = new StandardOptionRegistry<>();
        optionRegistry
                // Map Fluctlight arguments to Discord USER option (keep default USER->User mapping too)
                .registerMapping(JDAOptionType.USER, ParserDescriptor.of(fluctlightParser, Fluctlight.class))
                .registerMapping(JDAOptionType.USER, JDAParser.userParser())
                .registerMapping(JDAOptionType.CHANNEL, JDAParser.channelParser())
                .registerMapping(JDAOptionType.ROLE, JDAParser.roleParser())
                .registerMapping(JDAOptionType.MENTIONABLE, JDAParser.mentionableParser())
                .registerMapping(JDAOptionType.ATTACHMENT, JDAParser.attachmentParser());

        this.discordCommandFactory = new StandardDiscordCommandFactory<>(optionRegistry);
        this.nodeProcessor = new NodeProcessor<>(this.commandTree);
    }

    @Override
    public void commandScopePredicate(@NotNull CommandScopePredicate<Interaction> predicate) {
        this.commandScopePredicate = Objects.requireNonNull(predicate, "predicate");
    }

    @Override
    public @NotNull Collection<@NotNull CommandData> createCommands(@NotNull CommandScope<Interaction> scope) {
        this.nodeProcessor.prepareTree();

        List<CommandData> commands = new ArrayList<>();
        for (CommandNode<Interaction> rootNode : this.commandTree.rootNodes()) {
            CommandScope<Interaction> rootScope = (CommandScope<Interaction>) rootNode.nodeMeta().get(NodeProcessor.NODE_META_SCOPE);

            if (!rootScope.overlaps(scope)) continue;
            if (!this.commandScopePredicate.test(rootNode, scope)) continue;

            DiscordCommand<Interaction> command = this.discordCommandFactory.create(rootNode);
            Map<List<String>, CommandDefinition> definitionsByPath = buildDefinitionLookup(rootNode);
            CommandDefinition definition = definitionForPath(definitionsByPath, List.of(command.name()));
            SlashCommandData data = Commands.slash(command.name(), text(command.description()));

            // Apply options
            for (DiscordOption<Interaction> option : command.options()) {
                List<String> optionPath = new ArrayList<>(List.of(command.name(), option.name()));
                CommandDefinition optionDefinition = definitionForPath(definitionsByPath, optionPath);

                if (option instanceof DiscordOption.SubCommand) {
                    if (option.type().equals(DiscordOptionType.SUB_COMMAND)) {
                        data.addSubcommands(createSubCommand((DiscordOption.SubCommand<Interaction>) option, optionDefinition));
                        continue;
                    }

                    data.addSubcommandGroups(createSubCommandGroup(
                            (DiscordOption.SubCommand<Interaction>) option,
                            optionDefinition,
                            definitionsByPath,
                            optionPath
                    ));
                    continue;
                }

                data.addOptions(createOption((DiscordOption.Variable<Interaction>) option, definition));
            }

            // Apply permissions
            if (rootNode.command() != null) {
                Map<Type, Permission> accessMap = rootNode.nodeMeta().getOrNull(CommandNode.META_KEY_ACCESS);
                Type senderType = rootNode.command().senderType().map(TypeToken::getType).orElse(null);
                if (accessMap != null && senderType != null) {
                    Permission permission = accessMap.get(senderType);
                    if (permission instanceof DiscordPermission)
                        data.setDefaultPermissions(DefaultMemberPermissions
                                .enabledFor(((DiscordPermission) permission)
                                        .permission())
                        );
                }
            }

            // Apply scope context
            if (rootScope instanceof CommandScope.Guilds)
                data.setContexts(InteractionContextType.GUILD);

            // Descriptions are keys of texts, set in every locale they have
            localize(data, command.description());

            commands.add(data);
        }

        return commands;
    }

    /**
     * Sets a component's descriptions in every locale its text has: a description is the key of a text
     * ({@code commands.clear.description}), or plain text that stays as written.
     */
    private static void localize(@NotNull Object localizable, @Nullable String description) {
        if (description == null || description.isBlank()) return;

        Map<DiscordLocale, String> localizations = localizations(description);
        if (localizations.isEmpty()) return;

        switch (localizable) {
            case SlashCommandData data -> data.setDescriptionLocalizations(localizations);
            case SubcommandData data -> data.setDescriptionLocalizations(localizations);
            case SubcommandGroupData data -> data.setDescriptionLocalizations(localizations);
            case OptionData data -> data.setDescriptionLocalizations(localizations);
            default -> {
            }
        }
    }

    /**
     * Creates a subcommand with localization support.
     * Mirrors StandardJDACommandFactory.createSubCommand but adds localization.
     */
    private @NotNull SubcommandData createSubCommand(
            DiscordOption.SubCommand<Interaction> option,
            @Nullable CommandDefinition definition
    ) {
        SubcommandData subcommandData = new SubcommandData(option.name(), text(option.description()));
        localize(subcommandData, option.description());

        for (DiscordOption<Interaction> child : option.options()) {
            if (child instanceof DiscordOption.SubCommand)
                throw new IllegalArgumentException(
                        "Cannot add subcommand " + child.name() + " as a child of subcommand " + option.name()
                );

            OptionData childOption = createOption((DiscordOption.Variable<Interaction>) child, definition);
            subcommandData.addOptions(childOption);
        }

        return subcommandData;
    }

    /**
     * Creates a subcommand group with localization support.
     * Mirrors StandardJDACommandFactory.createSubCommandGroup but adds localization.
     */
    private @NotNull SubcommandGroupData createSubCommandGroup(
            DiscordOption.SubCommand<Interaction> option,
            @Nullable CommandDefinition definition,
            Map<List<String>, CommandDefinition> definitionsByPath,
            List<String> currentPath
    ) {
        SubcommandGroupData subcommandGroupData = new SubcommandGroupData(option.name(), text(option.description()));
        localize(subcommandGroupData, option.description());

        for (DiscordOption<Interaction> child : option.options()) {
            if (child instanceof DiscordOption.Variable)
                throw new IllegalArgumentException(
                        "Cannot add variable option " + child.name() + " as child of group " + option.name()
                );

            List<String> childPath = new ArrayList<>(currentPath);
            childPath.add(child.name());
            CommandDefinition childDefinition = definitionForPath(definitionsByPath, childPath);
            subcommandGroupData = subcommandGroupData.addSubcommands(
                    createSubCommand((DiscordOption.SubCommand<Interaction>) child, childDefinition)
            );
        }
        return subcommandGroupData;
    }

    /**
     * Creates an option with localization support.
     * Mirrors StandardJDACommandFactory.createOption but adds localization.
     */
    private @NotNull OptionData createOption(
            DiscordOption.Variable<Interaction> option,
            @Nullable CommandDefinition definition
    ) {
        OptionData optionData = new OptionData(
                OptionType.fromKey(option.type().value()),
                option.name(),
                text(option.description())
        ).setRequired(option.required());

        localize(optionData, option.description());

        // Standard option configuration (mirrors StandardJDACommandFactory)
        if (option.range() != null)
            optionData = optionData.setMinValue(option.range().min().longValue())
                    .setMaxValue(option.range().max().longValue());

        if (option.autocomplete()) {
            optionData = optionData.setAutoComplete(true);
        } else {
            optionData = optionData.addChoices(createChoices(option.choices()));
        }

        return optionData;
    }

    /**
     * Gets the CommandDefinition for the given root node.
     */
    @Nullable
    private CommandDefinition getDefinition(@NotNull CommandNode<Interaction> node) {
        if (node.command() == null) return null;

        String definitionId = node.command().commandMeta()
                .optional(YuiCommandMetaKeys.DEFINITION)
                .orElse(null);

        if (definitionId == null) return null;

        return definitionRegistry.get(definitionId).orElse(null);
    }

    /**
     * Builds a lookup map of command definition by literal path for a root node.
     */
    private Map<List<String>, CommandDefinition> buildDefinitionLookup(@NotNull CommandNode<Interaction> rootNode) {
        Map<List<String>, CommandDefinition> map = new LinkedHashMap<>();
        collectDefinitions(rootNode, new ArrayList<>(), map);
        return map;
    }

    private void collectDefinitions(
            @NotNull CommandNode<Interaction> node,
            @NotNull List<String> literalPath,
            @NotNull Map<List<String>, CommandDefinition> map
    ) {
        CommandComponent<Interaction> component = node.component();
        if (component != null && component.type() == CommandComponent.ComponentType.LITERAL)
            literalPath.add(component.name());

        if (node.command() != null) {
            CommandDefinition definition = getDefinition(node);
            if (definition != null)
                map.put(List.copyOf(literalPath), definition);
        }

        for (CommandNode<Interaction> child : node.children())
            collectDefinitions(child, new ArrayList<>(literalPath), map);
    }

    /**
     * Resolves the best matching definition for a given literal path.
     */
    private @Nullable CommandDefinition definitionForPath(
            @NotNull Map<List<String>, CommandDefinition> definitionsByPath,
            @NotNull List<String> literalPath
    ) {
        CommandDefinition direct = definitionsByPath.get(literalPath);
        if (direct != null) return direct;

        for (Map.Entry<List<String>, CommandDefinition> entry : definitionsByPath.entrySet()) {
            List<String> candidatePath = entry.getKey();
            if (candidatePath.size() < literalPath.size()) continue;

            if (candidatePath.subList(0, literalPath.size()).equals(literalPath))
                return entry.getValue();
        }

        return null;
    }

    /**
     * The description in Yui's default locale: its text, or the description itself when it is no key of one.
     */
    private static @NotNull String text(@Nullable String description) {
        if (description == null || description.isBlank()) return "";

        return Translatable.text(description).resolveDefault();
    }

    /**
     * The description in each Discord locale the text resolves in.
     */
    private static @NotNull Map<DiscordLocale, String> localizations(@NotNull String description) {
        Map<DiscordLocale, String> localizations = new HashMap<>();
        for (DiscordLocale locale : DiscordLocale.values()) {
            if (locale == DiscordLocale.UNKNOWN) continue;

            String translated = Translatable.text(description).resolve(locale);
            if (!translated.isBlank() && !translated.equals(description)) localizations.put(locale, translated);
        }

        return localizations;
    }

    /**
     * Creates choices from Discord option choices.
     */
    private @NotNull Collection<Command.Choice> createChoices(
            @NotNull Collection<DiscordOptionChoice<?>> choices
    ) {
        return choices.stream().map(choice -> {
            if (choice.value() instanceof Integer)
                return new Command.Choice(choice.name(), (int) choice.value());

            if (choice.value() instanceof Double)
                return new Command.Choice(choice.name(), (double) choice.value());

            return new Command.Choice(choice.name(), choice.value().toString());
        }).collect(Collectors.toList());
    }
}

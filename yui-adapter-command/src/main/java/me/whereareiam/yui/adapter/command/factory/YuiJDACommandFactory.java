package me.whereareiam.yui.adapter.command.factory;

import io.leangen.geantyref.TypeToken;
import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.translation.TranslationTags;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.discord.jda6.JDACommandFactory;
import org.incendo.cloud.discord.jda6.JDAOptionType;
import org.incendo.cloud.discord.jda6.JDAParser;
import org.incendo.cloud.discord.slash.*;
import org.incendo.cloud.internal.CommandNode;
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
 * when descriptions contain translate(...) tags.
 */
public final class YuiJDACommandFactory<C> implements JDACommandFactory<C> {
    private final CommandTree<C> commandTree;
    private final CommandDefinitionRegistry definitionRegistry;
    private final DiscordCommandFactory<C> discordCommandFactory;
    private final NodeProcessor<C> nodeProcessor;

    private CommandScopePredicate<C> commandScopePredicate = CommandScopePredicate.alwaysTrue();

    public YuiJDACommandFactory(@NotNull CommandTree<C> commandTree, @NotNull CommandDefinitionRegistry definitionRegistry) {
        this.commandTree = Objects.requireNonNull(commandTree, "commandTree");
        this.definitionRegistry = Objects.requireNonNull(definitionRegistry, "definitionRegistry");

        // Reuse Cloud's standard option registry setup
        OptionRegistry<C> optionRegistry = new StandardOptionRegistry<>();
        optionRegistry
                .registerMapping(JDAOptionType.USER, JDAParser.userParser())
                .registerMapping(JDAOptionType.CHANNEL, JDAParser.channelParser())
                .registerMapping(JDAOptionType.ROLE, JDAParser.roleParser())
                .registerMapping(JDAOptionType.MENTIONABLE, JDAParser.mentionableParser())
                .registerMapping(JDAOptionType.ATTACHMENT, JDAParser.attachmentParser());

        this.discordCommandFactory = new StandardDiscordCommandFactory<>(optionRegistry);
        this.nodeProcessor = new NodeProcessor<>(this.commandTree);
    }

    @Override
    public void commandScopePredicate(@NotNull CommandScopePredicate<C> predicate) {
        this.commandScopePredicate = Objects.requireNonNull(predicate, "predicate");
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Collection<@NotNull CommandData> createCommands(@NotNull CommandScope<C> scope) {
        this.nodeProcessor.prepareTree();

        List<CommandData> commands = new ArrayList<>();
        for (CommandNode<C> rootNode : this.commandTree.rootNodes()) {
            CommandScope<C> rootScope = (CommandScope<C>) rootNode.nodeMeta().get(NodeProcessor.NODE_META_SCOPE);

            if (!rootScope.overlaps(scope)) continue;
            if (!this.commandScopePredicate.test(rootNode, scope)) continue;

            DiscordCommand<C> command = this.discordCommandFactory.create(rootNode);
            SlashCommandData data = Commands.slash(command.name(), command.description());

            // Get definition once for this command
            CommandDefinition definition = getDefinition(rootNode);

            // Apply options
            for (DiscordOption<C> option : command.options()) {
                if (option instanceof DiscordOption.SubCommand) {
                    if (option.type().equals(DiscordOptionType.SUB_COMMAND)) {
                        data.addSubcommands(createSubCommand((DiscordOption.SubCommand<C>) option, definition));
                        continue;
                    }

                    data.addSubcommandGroups(createSubCommandGroup((DiscordOption.SubCommand<C>) option, definition));
                    continue;
                }

                data.addOptions(createOption((DiscordOption.Variable<C>) option, definition));
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

            // Apply localizations if definition exists and contains translate tags
            // Check both definition description and command description (which may differ)
            applyLocalizations(data, definition, command.description());

            commands.add(data);
        }

        return commands;
    }

    /**
     * Applies localizations to command data if description contains translate tags.
     * Priority: commandDescription > definitionDescription
     */
    private void applyLocalizations(SlashCommandData data, @Nullable CommandDefinition definition, @Nullable String commandDescription) {
        String descriptionToLocalize = commandDescription;
        if (descriptionToLocalize == null && definition != null)
            descriptionToLocalize = definition.getDescription();

        applyLocalization(data, descriptionToLocalize, definition, null);
    }

    /**
     * Generic method to apply localization to any localizable command component.
     * Priority: primaryDescription > variableDescription (for options) > definitionDescription
     */
    private void applyLocalization(
            @NotNull Object localizable,
            @Nullable String primaryDescription,
            @Nullable CommandDefinition definition,
            @Nullable String variableDescription
    ) {
        String descriptionToLocalize = null;

        // For options: check variable description first, then primary
        if (containsTranslateTag(variableDescription))
            descriptionToLocalize = variableDescription;

        if (containsTranslateTag(primaryDescription))
            descriptionToLocalize = primaryDescription;

        if (definition != null) {
            String definitionDescription = definition.getDescription();
            if (containsTranslateTag(definitionDescription))
                descriptionToLocalize = definitionDescription;
        }

        if (descriptionToLocalize == null) return;

        Map<DiscordLocale, String> localizations = buildLocalizations(descriptionToLocalize);
        if (localizations.isEmpty()) return;

        // Apply to the appropriate type
        if (localizable instanceof SlashCommandData data)
            data.setDescriptionLocalizations(localizations);

        if (localizable instanceof SubcommandData data)
            data.setDescriptionLocalizations(localizations);

        if (localizable instanceof SubcommandGroupData data)
            data.setDescriptionLocalizations(localizations);

        if (localizable instanceof OptionData data)
            data.setDescriptionLocalizations(localizations);
    }

    /**
     * Creates a subcommand with localization support.
     * Mirrors StandardJDACommandFactory.createSubCommand but adds localization.
     */
    private @NotNull SubcommandData createSubCommand(
            DiscordOption.SubCommand<C> option,
            @Nullable CommandDefinition definition
    ) {
        SubcommandData subcommandData = new SubcommandData(option.name(), option.description());
        applyLocalization(subcommandData, option.description(), definition, null);

        for (DiscordOption<C> child : option.options()) {
            if (child instanceof DiscordOption.SubCommand)
                throw new IllegalArgumentException(
                        "Cannot add subcommand " + child.name() + " as a child of subcommand " + option.name()
                );

            OptionData childOption = createOption((DiscordOption.Variable<C>) child, definition);
            subcommandData.addOptions(childOption);
        }

        return subcommandData;
    }

    /**
     * Creates a subcommand group with localization support.
     * Mirrors StandardJDACommandFactory.createSubCommandGroup but adds localization.
     */
    private @NotNull SubcommandGroupData createSubCommandGroup(
            DiscordOption.SubCommand<C> option,
            @Nullable CommandDefinition definition
    ) {
        SubcommandGroupData subcommandGroupData = new SubcommandGroupData(option.name(), option.description());
        applyLocalization(subcommandGroupData, option.description(), definition, null);

        for (DiscordOption<C> child : option.options()) {
            if (child instanceof DiscordOption.Variable)
                throw new IllegalArgumentException(
                        "Cannot add variable option " + child.name() + " as child of group " + option.name()
                );

            subcommandGroupData = subcommandGroupData.addSubcommands(
                    createSubCommand((DiscordOption.SubCommand<C>) child, definition)
            );
        }
        return subcommandGroupData;
    }

    /**
     * Creates an option with localization support.
     * Mirrors StandardJDACommandFactory.createOption but adds localization.
     */
    private @NotNull OptionData createOption(
            DiscordOption.Variable<C> option,
            @Nullable CommandDefinition definition
    ) {
        OptionData optionData = new OptionData(
                OptionType.fromKey(option.type().value()),
                option.name(),
                option.description()
        ).setRequired(option.required());

        // Apply localization - check variables map first, then option description
        String variableDescription = definition != null && definition.getVariables() != null
                ? definition.getVariables().get(option.name())
                : null;
        applyLocalization(optionData, option.description(), definition, variableDescription);

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
    private CommandDefinition getDefinition(@NotNull CommandNode<C> rootNode) {
        if (rootNode.command() == null) return null;

        String definitionId = rootNode.command().commandMeta()
                .optional(YuiCommandMetaKeys.DEFINITION)
                .orElse(null);

        if (definitionId == null) return null;

        return definitionRegistry.get(definitionId).orElse(null);
    }

    /**
     * Checks if a string contains translate(...) tags.
     */
    private boolean containsTranslateTag(@Nullable String text) {
        if (text == null || text.isBlank()) return false;
        return text.contains("translate(");
    }

    /**
     * Builds localizations map for all Discord locales.
     * Only includes locales where the translation resolves to a non-empty value.
     * Excludes UNKNOWN locale as it's not a valid Discord locale.
     */
    private @NotNull Map<DiscordLocale, String> buildLocalizations(@NotNull String text) {
        Map<DiscordLocale, String> localizations = new HashMap<>();

        for (DiscordLocale locale : DiscordLocale.values()) {
            // Skip UNKNOWN locale as JDA doesn't allow it in localizations
            if (locale == DiscordLocale.UNKNOWN) continue;

            String translated = TranslationTags.resolve(text, locale);
            // Only add if translation resolved to something meaningful
            // (not empty and not the same as the original translate tag)
            if (translated != null && !translated.isBlank() && !translated.equals(text))
                localizations.put(locale, translated);
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

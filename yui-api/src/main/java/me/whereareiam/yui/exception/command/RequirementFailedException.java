package me.whereareiam.yui.exception.command;

import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.command.exception.ExceptionResponse;
import me.whereareiam.yui.exception.command.base.CommandException;
import me.whereareiam.yui.model.requirement.Requirements;
import me.whereareiam.yui.model.requirement.type.*;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class RequirementFailedException extends CommandException {
	private final Requirements requirements;
	
	/**
	 * Creates a new requirement failed exception.
	 *
	 * @param requirements The requirements that failed
	 */
	public RequirementFailedException(@NotNull Requirements requirements) {
		super("Command requirements not met");
		this.requirements = requirements;
	}
	
	/**
	 * Returns the requirements that failed.
	 *
	 * @return The requirements
	 */
	@NotNull
	public Requirements getRequirements() {
		return requirements;
	}
	
	@Override
	@NotNull
	public ExceptionResponse createResponse(@NotNull Interaction interaction) {
		long userId = interaction.fluctlight().getId();
		
		String title = Translatable.text("commands.error.requirement.title").resolve(userId);
		String description = Translatable.text("commands.error.requirement.description").resolve(userId);

		EmbedBuilder embed = StyleKit.embeds().error()
				.setTitle(title)
				.setDescription(description);

		// Populate fields describing each failed requirement
		addRequirementFields(embed, userId);

		return ExceptionResponse.embed(embed);
	}

	/**
	 * Adds an embed field for each requirement entry using localized translations.
	 * Each resolved translation is expected to contain a short header on the first line
	 * (e.g. "**Required Role(s):**") followed by the details on subsequent lines.
	 */
	private void addRequirementFields(EmbedBuilder embed, long userId) {
		if (requirements == null || requirements.getGroups() == null || requirements.getGroups().isEmpty()) {
			String unknown = Translatable.text("commands.error.requirement.unknown").resolve(userId);
			// Put the unknown message into the description if no detailed fields exist
			embed.setDescription(unknown);
			return;
		}

		// Group entries by their concrete type
		List<RoleRequirement> roleReqs = requirements.getGroups().values().stream()
				.filter(e -> e instanceof RoleRequirement)
				.map(e -> (RoleRequirement) e)
				.toList();

		List<ScopeRequirement> scopeReqs = requirements.getGroups().values().stream()
				.filter(e -> e instanceof ScopeRequirement)
				.map(e -> (ScopeRequirement) e)
				.toList();

		List<ChannelTypeRequirement> channelReqs = requirements.getGroups().values().stream()
				.filter(e -> e instanceof ChannelTypeRequirement)
				.map(e -> (ChannelTypeRequirement) e)
				.toList();

		List<UserRequirement> userReqs = requirements.getGroups().values().stream()
				.filter(e -> e instanceof UserRequirement)
				.map(e -> (UserRequirement) e)
				.toList();

		List<GuildRequirement> guildReqs = requirements.getGroups().values().stream()
				.filter(e -> e instanceof GuildRequirement)
				.map(e -> (GuildRequirement) e)
				.toList();

		if (roleReqs.isEmpty() && scopeReqs.isEmpty() && channelReqs.isEmpty() && userReqs.isEmpty() && guildReqs.isEmpty()) {
			String unknown = Translatable.text("commands.error.requirement.unknown").resolve(userId);
			embed.setDescription(unknown);
			return;
		}

		// For each group, produce a single localized block and add as an embed field
		if (!roleReqs.isEmpty()) {
			// Combine all role lists across entries
			String combinedRoles = roleReqs.stream()
				.flatMap(r -> r.getRoles() == null ? java.util.stream.Stream.empty() : r.getRoles().stream())
				.collect(Collectors.joining(", "));
			String matchBy = roleReqs.stream()
				.map(r -> r.getRoleMatchBy() == null ? "id" : r.getRoleMatchBy().toLowerCase())
				.findFirst().orElse("id");
			String combinedPart = Translatable.text("commands.error.requirement.role")
				.with("roles", combinedRoles)
				.with("matchBy", matchBy)
				.resolve(userId);

			// Prefer grouped fields (name + value) if available
			String nameKey = "commands.error.requirement.fields.role.name";
			String valueKey = "commands.error.requirement.fields.role.value";
			String name = Translatable.text(nameKey).resolve(userId);
			String value = Translatable.text(valueKey).with("roles", combinedRoles).with("matchBy", matchBy).resolve(userId);
			if (name.equals(nameKey) || value.equals(valueKey)) {
				// Missing grouped keys -> fallback to combined representation
				addPartAsField(embed, combinedPart, userId);
			} else {
				embed.addField(name, value, false);
			}
		}

		if (!scopeReqs.isEmpty()) {
			String combinedScopes = scopeReqs.stream()
				.flatMap(s -> s.getScopes() == null ? java.util.stream.Stream.empty() : s.getScopes().stream())
				.collect(Collectors.joining(", "));
			String combinedPart = Translatable.text("commands.error.requirement.scope")
				.with("scopes", combinedScopes)
				.resolve(userId);

			String nameKey = "commands.error.requirement.fields.scope.name";
			String valueKey = "commands.error.requirement.fields.scope.value";
			String name = Translatable.text(nameKey).resolve(userId);
			String value = Translatable.text(valueKey).with("scopes", combinedScopes).resolve(userId);
			if (name.equals(nameKey) || value.equals(valueKey)) {
				addPartAsField(embed, combinedPart, userId);
			} else {
				embed.addField(name, value, false);
			}
		}

		if (!channelReqs.isEmpty()) {
			String combinedTypes = channelReqs.stream()
				.flatMap(c -> c.getTypes() == null ? java.util.stream.Stream.empty() : c.getTypes().stream())
				.collect(Collectors.joining(", "));
			String combinedPart = Translatable.text("commands.error.requirement.channel")
				.with("channelTypes", combinedTypes)
				.resolve(userId);

			String nameKey = "commands.error.requirement.fields.channel.name";
			String valueKey = "commands.error.requirement.fields.channel.value";
			String name = Translatable.text(nameKey).resolve(userId);
			String value = Translatable.text(valueKey).with("channelTypes", combinedTypes).resolve(userId);
			if (name.equals(nameKey) || value.equals(valueKey)) {
				addPartAsField(embed, combinedPart, userId);
			} else {
				embed.addField(name, value, false);
			}
		}

		if (!userReqs.isEmpty()) {
			String combinedUserIds = userReqs.stream()
				.flatMap(u -> u.getUserIds() == null ? java.util.stream.Stream.empty() : u.getUserIds().stream())
				.map(String::valueOf)
				.collect(Collectors.joining(", "));
			String combinedPart = Translatable.text("commands.error.requirement.user")
				.with("userIds", combinedUserIds)
				.resolve(userId);

			String nameKey = "commands.error.requirement.fields.user.name";
			String valueKey = "commands.error.requirement.fields.user.value";
			String name = Translatable.text(nameKey).resolve(userId);
			String value = Translatable.text(valueKey).with("userIds", combinedUserIds).resolve(userId);
			if (name.equals(nameKey) || value.equals(valueKey)) {
				addPartAsField(embed, combinedPart, userId);
			} else {
				embed.addField(name, value, false);
			}
		}

		if (!guildReqs.isEmpty()) {
			String combinedGuildIds = guildReqs.stream()
				.flatMap(g -> g.getGuildIds() == null ? java.util.stream.Stream.empty() : g.getGuildIds().stream())
				.map(String::valueOf)
				.collect(Collectors.joining(", "));
			String combinedPart = Translatable.text("commands.error.requirement.guild")
				.with("guildIds", combinedGuildIds)
				.resolve(userId);

			String nameKey = "commands.error.requirement.fields.guild.name";
			String valueKey = "commands.error.requirement.fields.guild.value";
			String name = Translatable.text(nameKey).resolve(userId);
			String value = Translatable.text(valueKey).with("guildIds", combinedGuildIds).resolve(userId);
			if (name.equals(nameKey) || value.equals(valueKey)) {
				addPartAsField(embed, combinedPart, userId);
			} else {
				embed.addField(name, value, false);
			}
		}
	}

	private void addPartAsField(EmbedBuilder embed, String part, long userId) {
		String[] lines = part.split("\\r?\\n", 2);
		String name = lines[0].trim();
		String value = lines.length > 1 ? lines[1].trim() : "";
		if (value.isEmpty()) {
			name = Translatable.text("commands.error.requirement.title").resolve(userId);
			value = part;
		}
		embed.addField(name, value, false);
	}
}

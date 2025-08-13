package me.whereareiam.yui.adapter.command.registrar;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.adapter.command.registrar.builder.SlashCommandBuilder;
import me.whereareiam.yui.api.model.command.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandRegistrar {
	private static final Logger log = LoggerFactory.getLogger(CommandRegistrar.class);
	private static final String MAIN_COMMAND_NAME = "main";

	private final JDA jda;
	private final SlashCommandBuilder builder;
	private final ConcurrentMap<String, Command> cache = new ConcurrentHashMap<>();

	public void registerDiscordCommand(String name, Command cmd) {
		if (cmd == null || !cmd.isEnabled()) cache.remove(name);
		else cache.put(name, cmd);
		syncWithDiscord();
	}

	public void registerDiscordCommands(Map<String, Command> cmds) {
		if (cmds != null) cmds.forEach((n, c) -> {
			if (c == null || !c.isEnabled()) cache.remove(n);
			else cache.put(n, c);
		});
		syncWithDiscord();
	}

	private void syncWithDiscord() {
		if (jda.getStatus() == JDA.Status.SHUTTING_DOWN || jda.getStatus() == JDA.Status.SHUTDOWN) {
			log.warn("Skipping syncWithDiscord: JDA is shutting down or already shut down.");
			return;
		}

		Map<String, SlashCommandData> roots = new HashMap<>();
		Map<String, List<SubcommandData>> subsByRoot = new HashMap<>();
		Set<String> subcommandAliases = new HashSet<>();
		List<String> mainAliases = new ArrayList<>();

		// 1-a) main (/yui …)
		Command mainCfg = cache.get(MAIN_COMMAND_NAME);
		if (mainCfg != null && mainCfg.isEnabled()) {
			for (String alias : mainCfg.getAliases()) {
				roots.put(alias, builder.buildMainCommand(alias, mainCfg));
				subsByRoot.put(alias, new ArrayList<>());
				mainAliases.add(alias);
			}
		}

		// 1-b) every other command
		cache.forEach((name, cfg) -> {
			if (!cfg.isEnabled() || MAIN_COMMAND_NAME.equals(name)) return;

			if (builder.isSubcommand(cfg)) {
				subcommandAliases.addAll(cfg.getAliases());
				if (!mainAliases.isEmpty()) {
					attachSubcommand(cfg, mainAliases, subsByRoot);
				} else {
					log.debug("Deferred sub-command '{}' – parent '/main' not registered yet.", name);
				}
			}

			String topLevelAlias = cfg.getAliases().isEmpty() ? name : cfg.getAliases().getFirst();
			roots.putIfAbsent(topLevelAlias, builder.buildStandaloneCommand(topLevelAlias, cfg));
			subsByRoot.putIfAbsent(topLevelAlias, new ArrayList<>());
		});

		// 2) add subcommand lists into each root's SlashCommandData
		for (String root : mainAliases) {
			List<SubcommandData> list = subsByRoot.get(root);
			if (list != null && !list.isEmpty()) {
				roots.get(root).addSubcommands(list);
			}
		}

		// 3) upsert each current command
		roots.forEach((alias, data) ->
				jda.upsertCommand(data).queue(
						cmd -> log.debug("Up-serted /{} (id={})", cmd.getName(), cmd.getId()),
						err -> log.error("Failed to up-sert /{}", alias, err))
		);

		// 4) collect the set of “wanted” names
		Set<String> wanted = roots.keySet().stream()
				.map(String::toLowerCase)
				.collect(Collectors.toSet());

		// 5) retrieve all registered slash commands and delete the orphans
		jda.retrieveCommands().queue(all -> all.stream()
				.filter(cmd -> cmd.getType() == net.dv8tion.jda.api.interactions.commands.Command.Type.SLASH)
				.filter(cmd -> !wanted.contains(cmd.getName().toLowerCase()))
				.forEach(orphan -> orphan.delete().queue(
						_ -> log.debug("Deleted stale /{}", orphan.getName()),
						err -> log.warn("Could not delete stale /{}: {}", orphan.getName(), err.getMessage())
				)));

		// 6) (Optional) also delete any disabled subcommands standing alone
		subcommandAliases.forEach(this::removeIfStandalone);
		cache.entrySet().stream()
				.filter(e -> !e.getValue().isEnabled())
				.forEach(e -> removeIfStandalone(e.getKey()));
	}

	private void attachSubcommand(
			Command cfg, List<String> mainAliases,
			Map<String, List<SubcommandData>> subsByRoot) {

		for (String subAlias : cfg.getAliases()) {
			SubcommandData sub = builder.buildSubcommand(subAlias, cfg);
			mainAliases.forEach(root -> subsByRoot.get(root).add(sub));
		}
	}

	private void removeIfStandalone(String slashName) {
		jda.retrieveCommands().queue(list ->
				list.stream()
						.filter(cmd ->
								cmd.getType() == net.dv8tion.jda.api.interactions.commands.Command.Type.SLASH &&
										cmd.getName().equalsIgnoreCase(slashName))
						.findFirst()
						.ifPresent(cmd -> cmd.delete().queue(
								__ -> log.debug("Deleted obsolete stand‑alone /{}", slashName),
								err -> log.warn("Could not delete /{} – {}", slashName, err.getMessage())))
		);
	}
}

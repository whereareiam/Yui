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
		// Reset cache to exactly the provided enabled commands
		cache.clear();
		if (cmds != null) {
			cmds.forEach((n, c) -> {
				if (c != null && c.isEnabled()) cache.put(n, c);
			});
		}
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
			} else {
				// Only add non-subcommands as standalone commands
				String topLevelAlias = cfg.getAliases().isEmpty() ? name : cfg.getAliases().getFirst();
				roots.putIfAbsent(topLevelAlias, builder.buildStandaloneCommand(topLevelAlias, cfg));
				subsByRoot.putIfAbsent(topLevelAlias, new ArrayList<>());
			}
		});

		// 2) add subcommand lists into each root's SlashCommandData
		for (String root : mainAliases) {
			List<SubcommandData> list = subsByRoot.get(root);
			if (list != null && !list.isEmpty()) {
				roots.get(root).addSubcommands(list);
			}
		}

		// 3) atomically overwrite the global command set (upserts + deletions)
		List<SlashCommandData> desiredList = new ArrayList<>(roots.values());
		jda.updateCommands()
				.addCommands(desiredList)
				.queue(
						updated -> log.debug("Synchronized {} slash command(s) with Discord", updated.size()),
						err -> log.error("Failed to synchronize slash commands", err)
				);
	}

	private void attachSubcommand(
			Command cfg, List<String> mainAliases,
			Map<String, List<SubcommandData>> subsByRoot) {

		for (String subAlias : cfg.getAliases()) {
			SubcommandData sub = builder.buildSubcommand(subAlias, cfg);
			mainAliases.forEach(root -> subsByRoot.get(root).add(sub));
		}
	}

	// Standalone deletions are no longer needed; bulk update handles removals.

	/**
	 * Clears all cached commands and syncs with Discord to remove them.
	 * This is used during reload to ensure a completely fresh start.
	 */
	public void clear() {
		log.debug("Clearing command registrar cache");
		cache.clear();
		syncWithDiscord();
	}
}

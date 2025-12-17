package me.whereareiam.yui.adapter.command.registrar;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.adapter.command.registrar.builder.SlashCommandBuilder;
import me.whereareiam.yui.model.command.Command;
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

		Map<String, SlashCommandData> roots = new LinkedHashMap<>();
		Map<String, List<SubcommandData>> subsByRoot = new HashMap<>();
		List<String> mainAliases = new ArrayList<>();

		// Helper to normalize alias names for Discord
		java.util.function.Function<String, String> norm = s ->
				(s == null ? "" : s.trim().toLowerCase());

		// 1-a) main (/yui …)
		Command mainCfg = cache.get(MAIN_COMMAND_NAME);
		if (mainCfg != null && mainCfg.isEnabled()) {
			List<String> aliases = mainCfg.getAliases() == null ? List.of() : mainCfg.getAliases();
			for (String aliasRaw : aliases) {
				String alias = norm.apply(aliasRaw);
				if (alias.isEmpty()) continue;

				roots.put(alias, builder.buildMainCommand(alias, mainCfg));
				subsByRoot.put(alias, new ArrayList<>());
				mainAliases.add(alias);
			}
		}

		// 1-b) every other command
		cache.forEach((name, cfg) -> {
			if (!cfg.isEnabled() || MAIN_COMMAND_NAME.equals(name)) return;

			if (builder.isSubcommand(cfg)) {
				if (!mainAliases.isEmpty()) {
					attachSubcommand(cfg, mainAliases, subsByRoot);
				} else {
					log.debug("Deferred sub-command '{}' – parent '/{}' not registered yet.", name, MAIN_COMMAND_NAME);
				}
				return;
			}

			// Standalone: register ALL aliases (or fallback to the config key if none)
			List<String> aliasesRaw = (cfg.getAliases() == null || cfg.getAliases().isEmpty())
					? List.of(name)
					: cfg.getAliases();

			// normalize + dedupe
			LinkedHashSet<String> aliases = new LinkedHashSet<>();
			for (String a : aliasesRaw) {
				String n = norm.apply(a);
				if (!n.isEmpty()) aliases.add(n);
			}
			if (aliases.isEmpty()) aliases.add(norm.apply(name));

			for (String alias : aliases) {
				if (roots.containsKey(alias)) {
					log.warn("Alias '{}' for command '{}' conflicts with an existing root command. Skipping this alias.", alias, name);
					continue;
				}
				SlashCommandData data = builder.buildStandaloneCommand(alias, cfg);
				roots.put(alias, data);
				// keep map in sync even if we won't attach subs to non-main, harmless
				subsByRoot.putIfAbsent(alias, new ArrayList<>());
			}
		});

		// 2) attach all subcommands to each main alias
		for (String root : mainAliases) {
			List<SubcommandData> list = subsByRoot.get(root);
			if (list != null && !list.isEmpty()) {
				roots.get(root).addSubcommands(list);
			}
		}

		// 3) upsert globally
		List<SlashCommandData> desiredList = new ArrayList<>(roots.values());

		if (log.isDebugEnabled()) {
			List<String> names = desiredList.stream().map(SlashCommandData::getName).toList();
			log.debug("Registering {} slash command(s): {}", names.size(), names);
		}

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
}

package me.whereareiam.yui.adapter.command.manager;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Extension of {@link JDA6CommandManager} that enables proper root command
 * deletion support for Yui by installing a custom {@code CommandRegistrationHandler}
 * and registering the {@link CloudCapability.StandardCapabilities#ROOT_COMMAND_DELETION}
 * capability.
 */
@Getter
public final class YuiCommandManager extends JDA6CommandManager<JDAInteraction> {
	public YuiCommandManager(
			final @NotNull ExecutionCoordinator<JDAInteraction> executionCoordinator,
			final @NotNull JDAInteraction.InteractionMapper<JDAInteraction> senderMapper,
			final @NotNull ScheduledExecutorService scheduledExecutorService,
			final @NotNull JDA jda
	) {
		super(executionCoordinator, senderMapper);

		// Enable root command deletion support and install a registration handler
		// that can react when Cloud deletes root commands.
		this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
		this.commandRegistrationHandler(new JDARegistrationHandler(scheduledExecutorService, this, jda));
	}
}


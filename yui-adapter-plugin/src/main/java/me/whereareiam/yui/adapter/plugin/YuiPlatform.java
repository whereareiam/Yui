package me.whereareiam.yui.adapter.plugin;

import me.whereareiam.chassis.platform.Platform;
import me.whereareiam.chassis.platform.PlatformPeer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Yui as Chassis's platform: a Discord bot, with nothing loaded beside it that a plugin could name as a peer.
 */
final class YuiPlatform implements Platform {
	@Override
	public @NotNull String name() {
		return "yui";
	}

	@Override
	public @NotNull Optional<PlatformPeer> peer(@NotNull String id) {
		return Optional.empty();
	}
}

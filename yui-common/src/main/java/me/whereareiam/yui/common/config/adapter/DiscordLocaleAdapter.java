package me.whereareiam.yui.common.config.adapter;

import me.whereareiam.configura.TypeAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

@Component
public class DiscordLocaleAdapter implements TypeAdapter<DiscordLocale> {
	@Override
	public DiscordLocale deserialize(String value) {
		if (value == null || value.isEmpty())
			return DiscordLocale.UNKNOWN;
        
		return DiscordLocale.from(value);
	}

	@Override
	public String serialize(DiscordLocale value) {
		if (value == null) return null;
		return value.getLocale();
	}
}


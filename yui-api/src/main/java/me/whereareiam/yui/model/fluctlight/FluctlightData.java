package me.whereareiam.yui.model.fluctlight;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * Data holder for Fluctlight custom data loaded from the database.
 * This class is used to transfer custom data between the database layer
 * and the business logic layer without exposing database entities.
 */
@Getter
public class FluctlightData {
	private final DiscordLocale primaryLanguage;
	private final DiscordLocale[] additionalLanguages;
	private final long[] allowedRoles;

	public FluctlightData(
			DiscordLocale primaryLanguage,
			DiscordLocale[] additionalLanguages,
			long[] allowedRoles
	) {
		this.primaryLanguage = primaryLanguage;
		this.additionalLanguages = additionalLanguages != null ? additionalLanguages : new DiscordLocale[0];
		this.allowedRoles = allowedRoles;
	}
}
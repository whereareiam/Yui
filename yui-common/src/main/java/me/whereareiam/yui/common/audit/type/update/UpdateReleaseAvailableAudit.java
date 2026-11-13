package me.whereareiam.yui.common.audit.type.update;

import me.whereareiam.yui.api.Constants;
import me.whereareiam.yui.api.event.update.UpdateAvailableEvent;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import me.whereareiam.yui.api.type.audit.AuditSeverity;
import me.whereareiam.yui.api.util.Audit;
import me.whereareiam.yui.api.util.style.StyleKit;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Audit handler for release update notifications.
 */
@Component
public class UpdateReleaseAvailableAudit {
    @EventListener
    public void onUpdateAvailableEvent(UpdateAvailableEvent event) {
        if (event.isDevBuild()) return;

        Audit.log(AuditCategory.SYSTEM, Constants.AuditEntries.UPDATE_AVAILABLE)
                .withSeverity(AuditSeverity.WARNING)
                .withLocalizedEmbed(locale -> {
                    String title = Translatable.text("audit.update.available.release.title")
                            .with("name", event.getComponentName())
                            .resolve(locale);
                    String description = Translatable.text("audit.update.available.release.description")
                            .with("name", event.getComponentName())
                            .with("current", event.getCurrentVersion())
                            .with("latest", event.getLatestVersion())
                            .resolve(locale);

                    EmbedBuilder embed = StyleKit.embeds().warning();
                    embed.setTitle(title);
                    embed.setDescription(description);
                    embed.addField(
                            Translatable.text("audit.update.available.release.fields.current").resolve(locale),
                            event.getCurrentVersion(),
                            true
                    );
                    embed.addField(
                            Translatable.text("audit.update.available.release.fields.latest").resolve(locale),
                            event.getLatestVersion(),
                            true
                    );
                    return embed.build();
                })
                .send();
    }
}

package me.whereareiam.yui.common.audit.type.update;

import me.whereareiam.yui.event.update.UpdateAvailableEvent;
import me.whereareiam.yui.type.AuditSeverity;
import me.whereareiam.yui.util.Audit;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
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

        Audit.log("update_" + event.getComponentId() + "_available")
                .withSeverity(AuditSeverity.WARNING)
                .withLocalizedEmbed(locale -> {
                    String title = Translatable.text("audit.update.available.title")
                            .with("name", event.getComponentName())
                            .resolve(locale);
                    String description = Translatable.text("audit.update.available.description")
                            .with("name", event.getComponentName())
                            .with("current", event.getCurrentVersion())
                            .with("latest", event.getLatestVersion())
                            .resolve(locale);

                    EmbedBuilder embed = StyleKit.embeds().warning();
                    embed.setTitle(title);
                    embed.setDescription(description);
                    embed.addField(
                            Translatable.text("audit.update.available.fields.current").resolve(locale),
                            event.getCurrentVersion(),
                            true
                    );
                    embed.addField(
                            Translatable.text("audit.update.available.fields.latest").resolve(locale),
                            event.getLatestVersion(),
                            true
                    );
                    return embed.build();
                })
                .send();
    }
}

package me.whereareiam.yui.common.audit.type.update;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.update.UpdateAvailableEvent;
import me.whereareiam.yui.type.AuditSeverity;
import me.whereareiam.yui.util.Audit;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Audit handler for dev builds that are behind the latest commits.
 */
@Slf4j
@Component
public class UpdateDevAvailableAudit {
    @EventListener
    public void onUpdateAvailableEvent(UpdateAvailableEvent event) {
        if (!event.isDevBuild()) return;

        Integer commitsBehind = event.getCommitsBehind();
        if (commitsBehind == null) {
            log.debug("Skipping dev build audit for {} - missing commitsBehind", event.getComponentId());
            return;
        }

        Audit.log("update_" + event.getComponentId() + "_behind")
                .withSeverity(AuditSeverity.WARNING)
                .withLocalizedEmbed(locale -> {
                    String title = Translatable.text("audit.update.behind.title")
                            .with("name", event.getComponentName())
                            .resolve(locale);
                    String description = Translatable.text("audit.update.behind.description")
                            .with("name", event.getComponentName())
                            .with("commits", String.valueOf(commitsBehind))
                            .resolve(locale);

                    EmbedBuilder embed = StyleKit.embeds().warning();
                    embed.setTitle(title);
                    embed.setDescription(description);
                    embed.addField(
                            Translatable.text("audit.update.behind.fields.commits").resolve(locale),
                            String.valueOf(commitsBehind),
                            true
                    );
                    embed.addField(
                            Translatable.text("audit.update.behind.fields.current").resolve(locale),
                            event.getCurrentVersion(),
                            true
                    );

                    return embed.build();
                })
                .send();
    }
}

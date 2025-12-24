package me.whereareiam.yui.common.audit.type.update;

import me.whereareiam.yui.event.update.UpdateLocalNotificationEvent;
import me.whereareiam.yui.type.AuditSeverity;
import me.whereareiam.yui.util.Audit;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UpdateLocalBuildAudit {
    @EventListener
    public void onLocalBuild(UpdateLocalNotificationEvent event) {
        Audit.log("update_" + event.getComponentId() + "_local_build")
                .withSeverity(AuditSeverity.WARNING)
                .withLocalizedEmbed(locale -> {
                    String title = Translatable.text("audit.update.local_build.title")
                            .with("name", event.getComponentName())
                            .resolve(locale);
                    String description = Translatable.text("audit.update.local_build.description")
                            .with("name", event.getComponentName())
                            .resolve(locale);

                    return StyleKit.embeds().warning()
                            .setTitle(title)
                            .setDescription(description)
                            .build();
                })
                .send();
    }
}

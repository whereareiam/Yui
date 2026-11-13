package me.whereareiam.yui.common.audit.type.update;

import me.whereareiam.yui.api.Constants;
import me.whereareiam.yui.api.event.update.UpdateLocalNotificationEvent;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import me.whereareiam.yui.api.type.audit.AuditSeverity;
import me.whereareiam.yui.api.util.Audit;
import me.whereareiam.yui.api.util.style.StyleKit;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UpdateLocalBuildAudit {
    @EventListener
    public void onLocalBuild(UpdateLocalNotificationEvent event) {
        Audit.log(AuditCategory.SYSTEM, Constants.AuditEntries.UPDATE_LOCAL_BUILD)
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

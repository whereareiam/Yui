package me.whereareiam.yui.api.plugin;

import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.yui.api.service.InteractionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * What a plugin's context holds of its own: Yui's services as the plugin uses them, under its id.
 */
@Configuration(proxyBeanMethods = false)
public class PluginBeans {
	@Bean
	@Primary
	public InteractionService pluginInteractions(ExtensionContext extension, ApplicationContext plugin) {
		InteractionService yui = plugin.getParent().getBean(InteractionService.class);

		return new PluginInteractionService(extension.descriptor().getId(), yui);
	}
}

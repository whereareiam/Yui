package me.whereareiam.yue.api.component;

import me.whereareiam.yue.api.input.ComponentIdManager;
import me.whereareiam.yue.api.model.ComponentInfo;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Components implements ApplicationContextAware {
	private static ComponentIdManager componentIdManager;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		componentIdManager = applicationContext.getBean(ComponentIdManager.class);
	}

	/**
	 * Gets the current component ID manager
	 */
	public static ComponentIdManager getManager() {
		if (componentIdManager == null) {
			throw new IllegalStateException("ComponentIdManager not initialized yet");
		}
		return componentIdManager;
	}

	// ---- Static convenience methods ----

	/**
	 * Registers a component
	 */
	public static String register(String pluginId, String componentType, String componentName) {
		return getManager().registerComponent(pluginId, componentType, componentName);
	}

	/**
	 * Registers a button
	 */
	public static String registerButton(String pluginId, String buttonName) {
		return getManager().registerButton(pluginId, buttonName);
	}

	// Add other register methods...

	public static Optional<ComponentInfo> getInfo(String componentId) {
		return getManager().getComponentInfo(componentId);
	}

	// ---- JDA Component Factory Methods ----

	/**
	 * Creates a primary button with an automatically registered ID
	 */
	public static Button primaryButton(String pluginId, String buttonName, String label) {
		String id = registerButton(pluginId, buttonName);
		return Button.primary(id, label);
	}

	/**
	 * Creates a secondary button with an automatically registered ID
	 */
	public static Button secondaryButton(String pluginId, String buttonName, String label) {
		String id = registerButton(pluginId, buttonName);
		return Button.secondary(id, label);
	}

	/**
	 * Creates a button with the specified style and an automatically registered ID
	 */
	public static Button button(ButtonStyle style, String pluginId, String buttonName, String label) {
		String id = registerButton(pluginId, buttonName);
		return Button.of(style, id, label);
	}

	/**
	 * Creates a StringSelectMenu builder with an automatically registered ID
	 */
	public static StringSelectMenu.Builder selectMenu(String pluginId, String menuName) {
		String id = getManager().registerSelectMenu(pluginId, menuName);
		return StringSelectMenu.create(id);
	}

	/**
	 * Creates a Modal builder with an automatically registered ID
	 */
	public static Modal.Builder modal(String pluginId, String modalName, String title) {
		String id = getManager().registerModal(pluginId, modalName);
		return Modal.create(id, title);
	}
}
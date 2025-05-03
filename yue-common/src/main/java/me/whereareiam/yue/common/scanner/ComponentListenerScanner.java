package me.whereareiam.yue.common.scanner;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.api.annotation.ComponentListener;
import me.whereareiam.yue.api.event.plugin.PluginEnabledEvent;
import me.whereareiam.yue.api.input.InteractionService;
import me.whereareiam.yue.api.model.plugin.InternalPlugin;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class ComponentListenerScanner {
	private final InteractionService interactions;
	private final ApplicationContext rootCtx;

	public void scan() {
		scan(rootCtx);
	}

	private void scan(ApplicationContext ctx) {
		for (String n : ctx.getBeanDefinitionNames()) {
			Object bean;
			try {
				bean = ctx.getBean(n);
			} catch (Exception e) {
				continue;
			}
			for (Method m : ReflectionUtils.getAllDeclaredMethods(bean.getClass())) {
				ComponentListener ann = AnnotatedElementUtils.findMergedAnnotation(m, ComponentListener.class);
				if (ann == null) continue;
				Class<?>[] p = m.getParameterTypes();
				if (p.length != 1 || !GenericComponentInteractionCreateEvent.class.isAssignableFrom(p[0])) continue;
				String path = ann.value();
				Class<?> type = p[0];
				m.setAccessible(true);
				Consumer<?> c = e -> {
					try {
						m.invoke(bean, e);
					} catch (Exception ignored) {
					}
				};
				interactions.registerHandler(path, (Class) type, (Consumer) c);
			}
		}
	}

	@EventListener
	public void onPluginEnabledEvent(PluginEnabledEvent event) {
		InternalPlugin plugin = event.getPlugin();
		scan(plugin.getContext());
	}
}
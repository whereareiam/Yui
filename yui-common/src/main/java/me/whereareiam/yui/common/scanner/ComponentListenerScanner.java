package me.whereareiam.yui.common.scanner;

import me.whereareiam.yui.api.annotation.ComponentListener;
import me.whereareiam.yui.api.input.InteractionService;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.function.Consumer;

@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class ComponentListenerScanner extends BaseContextScanner {
	private final InteractionService interactions;

	public ComponentListenerScanner(InteractionService interactions, ApplicationContext rootCtx) {
		super(rootCtx);
		this.interactions = interactions;
	}

	@Override
	public void scan(ApplicationContext ctx) {
		InteractionService localInteractions;
		try {
			localInteractions = ctx.getBean(InteractionService.class);
		} catch (Exception ignored) {
			localInteractions = this.interactions;
		}

		for (String n : ctx.getBeanDefinitionNames()) {
			Object bean;
			try {
				bean = ctx.getBean(n);
			} catch (Exception e) {
				continue;
			}

			for (Method m : ReflectionUtils.getAllDeclaredMethods(bean.getClass())) {
				ComponentListener ann = AnnotatedElementUtils.findMergedAnnotation(m, ComponentListener.class);
				if (ann == null)
					continue;

				Class<?>[] p = m.getParameterTypes();
				if (p.length != 1 || !GenericComponentInteractionCreateEvent.class.isAssignableFrom(p[0]))
					continue;

				String path = ann.value();
				Class<?> type = p[0];
				m.setAccessible(true);
				Consumer<?> c = e -> {
					try {
						m.invoke(bean, e);
					} catch (Exception ignored) {
					}
				};

				localInteractions.registerHandler(path, (Class) type, (Consumer) c);
			}
		}
	}
}
package me.whereareiam.yui.common.scanner;

import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.service.InteractionService;
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
	private final FluctlightService fluctlightService;

	public ComponentListenerScanner(InteractionService interactions, ApplicationContext rootCtx, FluctlightService fluctlightService) {
		super(rootCtx);
		this.interactions = interactions;
		this.fluctlightService = fluctlightService;
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
				
				// Support both 1 parameter (event) and 2 parameters (Fluctlight, event)
				boolean hasEventParam = p.length >= 1 && GenericComponentInteractionCreateEvent.class.isAssignableFrom(p[p.length - 1]);
				boolean hasFluctlightParam = p.length == 2 && p[0] == Fluctlight.class;
				
				if (!hasEventParam || (p.length != 1 && !hasFluctlightParam))
					continue;

				String path = ann.value();
				Class<?> type = p[p.length - 1];
				m.setAccessible(true);
				
				Consumer<?> c = hasFluctlightParam
						? createFluctlightConsumer(bean, m)
						: createEventConsumer(bean, m);

				localInteractions.registerHandler(path, (Class) type, (Consumer) c);
			}
		}
	}

	private Consumer<?> createEventConsumer(Object bean, Method method) {
		return e -> {
			try {
				method.invoke(bean, e);
			} catch (Exception ignored) {
			}
		};
	}

	private Consumer<?> createFluctlightConsumer(Object bean, Method method) {
		return e -> {
			try {
				GenericComponentInteractionCreateEvent event = (GenericComponentInteractionCreateEvent) e;
				long userId = event.getUser().getIdLong();
				Fluctlight fluctlight = fluctlightService.getOrCreate(userId);
				method.invoke(bean, fluctlight, e);
			} catch (Exception ignored) {
			}
		};
	}
}
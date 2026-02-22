package me.whereareiam.yui.common.scanner;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.annotation.journey.JourneyConfiguration;
import me.whereareiam.yui.annotation.journey.JourneyGroup;
import me.whereareiam.yui.annotation.journey.JourneyStep;
import me.whereareiam.yui.common.journey.JourneyDefinitionRegistry;
import me.whereareiam.yui.journey.definition.JourneyConfigurationDefinition;
import me.whereareiam.yui.journey.definition.group.JourneyGroupDefinition;
import me.whereareiam.yui.journey.definition.group.JourneyStepDefinition;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings("unchecked")
public class JourneyAnnotationScanner extends BaseContextScanner {
	private final JourneyDefinitionRegistry definitionRegistry;

	public JourneyAnnotationScanner(ApplicationContext rootCtx, JourneyDefinitionRegistry definitionRegistry) {
		super(rootCtx);
		this.definitionRegistry = definitionRegistry;
	}

	@Override
	public void scan(ApplicationContext ctx) {
		for (String beanName : ctx.getBeanDefinitionNames()) {
			Object bean;
			try {
				bean = ctx.getBean(beanName);
			} catch (Exception ignored) {
				continue;
			}

			Class<?> targetClass = AopUtils.getTargetClass(bean);
			JourneyStep stepAnnotation = AnnotatedElementUtils.findMergedAnnotation(targetClass, JourneyStep.class);
			if (stepAnnotation != null) {
				if (!(bean instanceof JourneyStepDefinition<?> stepDefinition))
					throw new IllegalStateException("Bean annotated with @JourneyStep must implement JourneyStepDefinition: " + targetClass.getName());

				definitionRegistry.registerStep(
						ctx,
						stepAnnotation.journeyId(),
						stepAnnotation.stepId(),
						stepAnnotation.order(),
						stepAnnotation.groupId(),
						(JourneyStepDefinition<Object>) stepDefinition
				);
				log.debug("Registered journey step {}:{}", stepAnnotation.journeyId(), stepAnnotation.stepId());
			}

			JourneyGroup groupAnn = AnnotatedElementUtils.findMergedAnnotation(targetClass, JourneyGroup.class);
			if (groupAnn != null) {
				if (!(bean instanceof JourneyGroupDefinition<?> groupDefinition))
					throw new IllegalStateException("Bean annotated with @JourneyGroup must implement JourneyGroupDefinition: " + targetClass.getName());

				definitionRegistry.registerGroup(
						ctx,
						groupAnn.journeyId(),
						groupAnn.groupId(),
						groupAnn.order(),
						(JourneyGroupDefinition<Object>) groupDefinition
				);
				log.debug("Registered journey group {}:{}", groupAnn.journeyId(), groupAnn.groupId());
			}

			JourneyConfiguration configAnn = AnnotatedElementUtils.findMergedAnnotation(targetClass, JourneyConfiguration.class);
			if (configAnn != null) {
				JourneyConfigurationDefinition configuration;
				if (bean instanceof JourneyConfigurationDefinition definition) {
					configuration = new AnnotationMergedConfigurationDefinition(definition, configAnn);
				} else {
					configuration = new AnnotationOnlyConfigurationDefinition(configAnn);
				}

				definitionRegistry.registerConfiguration(ctx, configAnn.journeyId(), configuration);
				log.debug("Registered journey configuration for {}", configAnn.journeyId());
			}
		}
	}

	private record AnnotationMergedConfigurationDefinition(
			JourneyConfigurationDefinition delegate,
			JourneyConfiguration annotation
	) implements JourneyConfigurationDefinition {
		@Override
		public String sessionStore() {
			if (annotation.sessionStore() != null && !annotation.sessionStore().isBlank())
				return annotation.sessionStore();

			return delegate.sessionStore();
		}
	}

	private record AnnotationOnlyConfigurationDefinition(JourneyConfiguration annotation)
			implements JourneyConfigurationDefinition {
		@Override
		public String sessionStore() {
			if (annotation.sessionStore() == null || annotation.sessionStore().isBlank())
				return null;

			return annotation.sessionStore();
		}
	}
}

package me.whereareiam.yui.adapter.command.registration;

import me.whereareiam.yui.annotation.command.Command;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.beans.factory.BeanFactoryUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Utility for locating Yui command containers in a Spring context.
 * <p>
 * A command container is any bean whose concrete class is annotated with
 * {@link me.whereareiam.yui.annotation.command.Command} at the type level.
 * Method-level {@link Command} annotations are discovered later by the
 * Cloud/Yui annotation parser.
 */
@Component
public class CommandScanner {
	/**
	 * Finds all command container beans in the given bean factory (including ancestors).
	 *
	 * @param beanFactory the bean factory to scan
	 * @return collection of command container instances
	 */
	public @NotNull Collection<Object> findCommand(@NotNull ListableBeanFactory beanFactory) {
		Map<String, Object> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				beanFactory,
				Object.class
		);

		Collection<Object> containers = new ArrayList<>();
		for (Object bean : beans.values()) {
			Class<?> targetClass = AopUtils.getTargetClass(bean);

			// Type-level @Command marks this bean as a command container
			if (AnnotationUtils.findAnnotation(targetClass, Command.class) != null)
				containers.add(bean);
		}

		return containers;
	}
}


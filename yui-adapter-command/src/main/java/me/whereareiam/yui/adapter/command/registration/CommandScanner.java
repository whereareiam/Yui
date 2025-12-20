package me.whereareiam.yui.adapter.command.registration;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.annotation.command.Command;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.beans.factory.BeanFactoryUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Utility for locating Yui command containers in a Spring context.
 * <p>
 * A command container is any bean that:
 * <ul>
 *   <li>Has a class-level {@link Command} annotation, OR</li>
 *   <li>Has at least one method annotated with {@link Command}</li>
 * </ul>
 * Method-level {@link Command} annotations are discovered later by the
 * Cloud/Yui annotation parser.
 */
@Component
@RequiredArgsConstructor
public class CommandScanner {
	private final ApplicationContext ctx;

	public Optional<Object> findCommand(@NotNull Class<?> commandClass) {
		Collection<Object> containers = findCommand(ctx);
		for (Object container : containers) {
			Class<?> targetClass = AopUtils.getTargetClass(container);
			if (targetClass.equals(commandClass)) {
				return Optional.of(container);
			}
		}

		return Optional.empty();
	}

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

			// Check for type-level @Command annotation
			if (AnnotationUtils.findAnnotation(targetClass, Command.class) != null) {
				containers.add(bean);
				continue;
			}

			// Check for methods annotated with @Command
			for (Method method : targetClass.getDeclaredMethods()) {
				if (AnnotationUtils.findAnnotation(method, Command.class) != null) {
					containers.add(bean);
					break;
				}
			}
		}

		return containers;
	}
}


package me.whereareiam.yui.api.annotation.journey;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JourneyStep {
	String journeyId();

	String stepId();

	int order() default 0;

	String groupId() default "";
}

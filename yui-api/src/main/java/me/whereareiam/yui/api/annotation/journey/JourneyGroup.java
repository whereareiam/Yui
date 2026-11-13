package me.whereareiam.yui.api.annotation.journey;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JourneyGroup {
	String journeyId();

	String groupId();

	int order() default 0;
}

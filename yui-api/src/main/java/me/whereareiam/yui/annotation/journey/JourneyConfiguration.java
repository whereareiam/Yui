package me.whereareiam.yui.annotation.journey;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JourneyConfiguration {
	String journeyId();

	String sessionStore() default "";
}

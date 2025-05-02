package me.whereareiam.yue.api.event;

import org.springframework.context.ApplicationEvent;

public abstract class YueEvent extends ApplicationEvent {
	public YueEvent(Object source) {
		super(source);
	}
}

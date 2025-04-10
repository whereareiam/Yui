package me.whereareiam.yue.common;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListenerRegistrar {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final List<ListenerAdapter> listeners;
	private final JDA jda;

	@Autowired
	public ListenerRegistrar(List<ListenerAdapter> listeners, JDA jda) {
		this.listeners = listeners;
		this.jda = jda;
	}

	@PostConstruct
	public void registerListeners() {
		for (ListenerAdapter listener : listeners) {
			jda.addEventListener(listener);
			logger.debug("Registered listener: {}", listener.getClass().getSimpleName());
		}
	}
}
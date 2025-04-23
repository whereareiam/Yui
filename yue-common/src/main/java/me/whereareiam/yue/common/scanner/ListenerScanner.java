package me.whereareiam.yue.common.scanner;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListenerScanner {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final List<ListenerAdapter> listeners;
	private final JDA jda;

	@Autowired
	public ListenerScanner(List<ListenerAdapter> listeners, JDA jda) {
		this.listeners = listeners;
		this.jda = jda;
	}

	public void scan() {
		for (ListenerAdapter listener : listeners) {
			jda.addEventListener(listener);
			logger.debug("Registered listener: {}", listener.getClass().getSimpleName());
		}
	}
}
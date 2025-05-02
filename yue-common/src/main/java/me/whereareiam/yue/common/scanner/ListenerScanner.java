package me.whereareiam.yue.common.scanner;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ListenerScanner {
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
			log.debug("Registered listener: {}", listener.getClass().getSimpleName());
		}
	}
}
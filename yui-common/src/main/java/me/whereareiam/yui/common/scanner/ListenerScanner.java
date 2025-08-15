package me.whereareiam.yui.common.scanner;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ListenerScanner extends BaseContextScanner {
	private final JDA jda;

	public ListenerScanner(ApplicationContext rootCtx, JDA jda) {
		super(rootCtx);
		this.jda = jda;
	}

	@Override
	public void scan(ApplicationContext context) {
		for (ListenerAdapter listener : context.getBeansOfType(ListenerAdapter.class).values()) {
			jda.addEventListener(listener);
			log.debug("Registered listener: {}", listener.getClass().getSimpleName());
		}
	}
}
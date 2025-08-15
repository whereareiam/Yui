package me.whereareiam.yui.common.scanner;

import org.springframework.context.ApplicationContext;

/**
 * Base scanner providing a root application context and a convenience no-arg scan method
 * that delegates to {@link #scan(ApplicationContext)} using the root context.
 */
public abstract class BaseContextScanner {
	protected final ApplicationContext rootCtx;

	protected BaseContextScanner(ApplicationContext rootCtx) {
		this.rootCtx = rootCtx;
	}

	public void scan() {
		scan(rootCtx);
	}

	public abstract void scan(ApplicationContext ctx);
}


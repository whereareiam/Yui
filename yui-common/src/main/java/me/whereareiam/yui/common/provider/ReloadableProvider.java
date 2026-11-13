package me.whereareiam.yui.common.provider;

import me.whereareiam.yui.api.Registry;
import me.whereareiam.yui.api.Reloadable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class ReloadableProvider implements Registry<Reloadable> {
	// Plugins register and unregister while /reload walks them.
	private final Set<Reloadable> reloadables = Collections.synchronizedSet(new LinkedHashSet<>());

	@Override
	public void register(Reloadable reloadable) {
		reloadables.add(reloadable);
	}

	@Override
	public void unregister(Reloadable reloadable) {
		reloadables.remove(reloadable);
	}

	@Override
	public java.util.Collection<Reloadable> getAll() {
		synchronized (reloadables) {
			return List.copyOf(reloadables);
		}
	}
}

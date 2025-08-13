package me.whereareiam.yui.common.provider;

import lombok.Getter;
import me.whereareiam.yui.api.input.Registry;
import me.whereareiam.yui.api.output.Reloadable;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class ReloadableProvider implements Registry<Reloadable> {
	private final Set<Reloadable> reloadables = new LinkedHashSet<>();

	@Override
	public void register(Reloadable reloadable) {
		reloadables.add(reloadable);
	}

	@Override
	public java.util.Collection<Reloadable> getAll() {
		return Set.copyOf(reloadables);
	}
}

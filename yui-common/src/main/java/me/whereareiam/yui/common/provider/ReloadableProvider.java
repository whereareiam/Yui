package me.whereareiam.yui.common.provider;

import lombok.Getter;
import me.whereareiam.yui.api.input.Registry;
import me.whereareiam.yui.api.output.Reloadable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Getter
@Component
public class ReloadableProvider implements Registry<Reloadable> {
	private final Set<Reloadable> reloadables = new HashSet<>();

	@Override
	public void register(Reloadable reloadable) {
		reloadables.add(reloadable);
	}
}

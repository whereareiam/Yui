package me.whereareiam.yue.common.provider;

import lombok.Getter;
import me.whereareiam.yue.api.Reloadable;
import me.whereareiam.yue.api.input.Registry;
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

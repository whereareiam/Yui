package me.whereareiam.yui.common.permission;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.conditia.Conditions;
import me.whereareiam.yui.api.Registry;
import me.whereareiam.yui.api.Reloadable;
import me.whereareiam.yui.api.model.config.audience.Audiences;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Names the audiences of {@code audiences.yml} in Conditia, so {@code audience: moderator} refers to one, and names
 * them again when the file reloads; an audience the file no longer has is forgotten, and the places that use it
 * deny until it comes back.
 */
@Component
@RequiredArgsConstructor
public class AudienceNames implements Reloadable {
	private final Conditions conditions;
	private final ObjectProvider<Audiences> audiences;
	private final Registry<Reloadable> reloadables;
	private final Set<String> named = new HashSet<>();

	@PostConstruct
	void start() {
		reloadables.register(this);
		reload();
	}

	@Override
	public synchronized void reload() {
		Audiences current = audiences.getObject();
		named.stream().filter(name -> !current.all().containsKey(name)).forEach(conditions::forget);
		named.clear();

		current.all().forEach((name, requires) -> {
			conditions.name(name, requires);
			named.add(name);
		});
	}
}

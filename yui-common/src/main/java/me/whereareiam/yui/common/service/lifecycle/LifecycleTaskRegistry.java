package me.whereareiam.yui.common.service.lifecycle;

import me.whereareiam.yui.Registry;
import me.whereareiam.yui.LifecycleTask;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LifecycleTaskRegistry implements Registry<LifecycleTask> {
	private final Map<String, LifecycleTask> tasksByName = new LinkedHashMap<>();

	@Override
	public void register(LifecycleTask task) {
		if (task == null || task.getName() == null || task.getName().isBlank())
			return;

		tasksByName.put(task.getName(), task);
	}

	@Override
	public Collection<LifecycleTask> getAll() {
		return tasksByName.values();
	}

	public Map<String, LifecycleTask> getAllByName() {
		return Map.copyOf(tasksByName);
	}
}



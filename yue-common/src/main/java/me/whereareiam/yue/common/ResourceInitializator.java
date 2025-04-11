package me.whereareiam.yue.common;

import me.whereareiam.yue.api.output.ResourceInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceInitializator {
	@Autowired
	public ResourceInitializator(List<ResourceInitializer> initializers) {
		for (ResourceInitializer initializer : initializers) {
			initializer.initialize();
		}
	}
}

package me.whereareiam.yue.adapter.module.resolver;

import me.whereareiam.yue.api.model.module.InternalModule;

public interface ModuleResolver {
	boolean resolve(InternalModule module);
}
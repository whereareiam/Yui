package me.whereareiam.yui.api.message;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * The texts of owners beside Yui, such as plugins: each owner's defaults, then its own folder of
 * {@code <locale>/} files over them, every key starting with the owner's id. Adding an owner again replaces its
 * texts at once; removing it takes them away.
 */
public interface MessageLayers {
	/**
	 * @param owner    the owner's id, the first segment of its keys, such as {@code verification}
	 * @param defaults its default texts
	 * @param folder   its folder of {@code <locale>/} files, read only; it need not exist
	 */
	void add(@NotNull String owner, @NotNull List<? extends MessageDefaults<?>> defaults, @NotNull Path folder);

	/**
	 * @param owner the owner's id
	 */
	void remove(@NotNull String owner);
}

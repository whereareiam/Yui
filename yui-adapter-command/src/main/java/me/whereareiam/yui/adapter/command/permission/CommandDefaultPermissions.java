package me.whereareiam.yui.adapter.command.permission;

import lombok.RequiredArgsConstructor;
import me.whereareiam.conditia.model.Node;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.model.config.audience.Audiences;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * The Discord permissions a command is registered with, so Discord hides it from members without them: every line of
 * its {@code requires:} that comes down to Discord permissions alone ({@code permission: MANAGE_SERVER}, or an
 * audience such as {@code operator} that is only that). Members without them would be refused anyway; a line Discord
 * cannot say, such as an {@code any}, is left to Yui's check on use.
 */
@Component
@RequiredArgsConstructor
public class CommandDefaultPermissions {
	private final ObjectProvider<Audiences> audiences;

	/**
	 * @param definition a command's definition
	 * @return the permissions Discord should ask for, or empty when no line comes down to them
	 */
	public @NotNull Optional<EnumSet<Permission>> of(@NotNull CommandDefinition definition) {
		EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
		for (Node line : definition.getRequires().getConditions().items())
			permissions(line, new HashSet<>()).ifPresent(permissions::addAll);

		return permissions.isEmpty() ? Optional.empty() : Optional.of(permissions);
	}

	// What one line comes down to: Discord permissions alone, or nothing Discord can say.
	private Optional<Set<Permission>> permissions(Node line, Set<String> seen) {
		if (!(line instanceof Node.Mapping mapping) || mapping.entries().size() != 1) return Optional.empty();

		String type = mapping.entries().firstEntry().getKey();
		Node value = mapping.entries().firstEntry().getValue();
		return switch (type) {
			case "permission" -> names(value);
			case "audience" -> audience(value, seen);
			default -> Optional.empty();
		};
	}

	private Optional<Set<Permission>> audience(Node value, Set<String> seen) {
		if (!(value instanceof Node.Scalar(String name)) || !seen.add(name)) return Optional.empty();

		Requirements list = audiences.getObject().all().get(name);
		if (list == null) return Optional.empty();

		Set<Permission> permissions = new HashSet<>();
		for (Node line : list.getConditions().items()) {
			Optional<Set<Permission>> reduced = permissions(line, seen);
			if (reduced.isEmpty()) return Optional.empty();

			permissions.addAll(reduced.get());
		}

		return Optional.of(permissions);
	}

	private static Optional<Set<Permission>> names(Node value) {
		Set<Permission> permissions = new HashSet<>();
		Iterable<Node> items = value instanceof Node.Sequence(var list) ? list : List.of(value);
		for (Node item : items) {
			if (!(item instanceof Node.Scalar(String text))) return Optional.empty();

			try {
				permissions.add(Permission.valueOf(text.toUpperCase(Locale.ROOT)));
			} catch (IllegalArgumentException unknown) {
				return Optional.empty();
			}
		}

		return Optional.of(permissions);
	}
}

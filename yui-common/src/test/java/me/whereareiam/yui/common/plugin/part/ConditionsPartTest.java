package me.whereareiam.yui.common.plugin.part;

import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.chassis.model.ExtensionDescriptor;
import me.whereareiam.conditia.Conditions;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.api.permission.PermissionParts;
import me.whereareiam.yui.api.type.ConfigurationType;
import me.whereareiam.yui.common.config.ConfiguraConfiguration;
import me.whereareiam.yui.common.permission.PermissionConfiguration;
import me.whereareiam.yui.common.permission.type.member.PermissionCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.support.GenericApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionsPartTest {
	@TempDir
	Path folder;

	@Test
	void aPluginsAudiences_AreNamedUnderItsId() throws Exception {
		Files.writeString(folder.resolve("audiences.yml"), "staff:\n  - permission: MANAGE_ROLES\n");
		Configura configura = ConfiguraConfiguration.build(ConfigurationType.YAML);
		Conditions conditions = new PermissionConfiguration().conditions(configura, List.of(new PermissionCondition()));
		ExtensionContext extension = extension("verification");
		GenericApplicationContext plugin = new GenericApplicationContext();
		plugin.refresh();

		AutoCloseable scope = new ConditionsPart(conditions, configura).attach(plugin, extension);
		Requirements requires = configura.mapper().readValue("- audience: verification:staff", Requirements.class);

		assertDoesNotThrow(() -> conditions.deferredPlace("test.requires", requires, PermissionParts.ACTOR, PermissionParts.INTERACTION));
		scope.close();
	}

	private ExtensionContext extension(String id) {
		ExtensionDescriptor descriptor = mock(ExtensionDescriptor.class);
		when(descriptor.getId()).thenReturn(id);
		ExtensionContext extension = mock(ExtensionContext.class);
		when(extension.descriptor()).thenReturn(descriptor);
		when(extension.dataFolder()).thenReturn(folder);

		return extension;
	}
}

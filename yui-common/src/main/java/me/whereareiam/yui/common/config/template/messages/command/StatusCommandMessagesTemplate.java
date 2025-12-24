package me.whereareiam.yui.common.config.template.messages.command;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.messages.command.StatusCommandMessages;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatusCommandMessagesTemplate implements TemplateProvider<StatusCommandMessages> {
	@Override
	public StatusCommandMessages supply(StatusCommandMessages status) {
		status.setDescription("Show Yui runtime environment and debug information");
		status.setExample("/yui status");
		StatusCommandMessages.Overview overview = new StatusCommandMessages.Overview();
		overview.setTitle("Yui Status");
		overview.setDescription(List.of(
				"Runtime status and diagnostics for Yui.",
				"Use this to verify environment, plugins, and localization.",
				"",
				"<plugins>"
		));

		StatusCommandMessages.Plugins plugins = new StatusCommandMessages.Plugins();
		plugins.setTitle("**Plugins**");
		plugins.setEmpty("No plugins loaded");
		plugins.setFormat("<name> v<version> - <authors> (id: <id>) (jar: <jar>) [<status>]");
		StatusCommandMessages.Plugins.Status pluginStatus = new StatusCommandMessages.Plugins.Status();
		pluginStatus.setEnabled("ENABLED");
		pluginStatus.setDisabled("DISABLED");
		pluginStatus.setLoadable("LOADABLE");
		plugins.setStatus(pluginStatus);
		overview.setPlugins(plugins);

		StatusCommandMessages.Fields fields = new StatusCommandMessages.Fields();

		StatusCommandMessages.Fields.Memory memory = new StatusCommandMessages.Fields.Memory();
		memory.setTitle("Memory");
		memory.setValue(List.of(
				"Used: <p:used> / <p:allocated> (<p:percent>%)",
				"Max: <p:max>"
		));
		fields.setMemory(memory);

		StatusCommandMessages.Fields.Cpu cpu = new StatusCommandMessages.Fields.Cpu();
		cpu.setTitle("CPU");
		cpu.setValue(List.of(
				"Process: <p:process>",
				"Cores: <p:cores>"
		));
		fields.setCpu(cpu);

		StatusCommandMessages.Fields.Java java = new StatusCommandMessages.Fields.Java();
		java.setTitle("Java");
		java.setValue(List.of(
				"Runtime: <p:runtime>",
				"Vendor: <p:vendor>",
				"VM: <p:vm>"
		));
		fields.setJava(java);

		StatusCommandMessages.Fields.Os os = new StatusCommandMessages.Fields.Os();
		os.setTitle("OS");
		os.setValue(List.of(
				"System: <p:system>"
		));
		fields.setOs(os);

		StatusCommandMessages.Fields.Locale locale = new StatusCommandMessages.Fields.Locale();
		locale.setTitle("Locale");
		locale.setValue(List.of(
				"Default: <p:default>",
				"Available: <p:available>"
		));
		fields.setLocale(locale);

		overview.setFields(fields);
		status.setOverview(overview);

		return status;
	}
}

package me.whereareiam.yui.api.persistence;

import jakarta.persistence.EntityManagerFactory;
import me.whereareiam.chassis.ExtensionContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.util.ClassUtils;

import java.util.Map;

/**
 * The persistence unit and transactions of a plugin that carries {@link Tables}, in the plugin's own context. Yui's
 * {@link PluginDatabase} opens the unit; the context closes it when the plugin stops.
 */
@Configuration(proxyBeanMethods = false)
public class PluginTables implements ImportAware {
	/**
	 * The name of the plugin's entity manager factory.
	 */
	public static final String ENTITY_MANAGER_FACTORY = "pluginEntityManagerFactory";

	/**
	 * The name of the plugin's transaction manager, also the one {@code @Transactional} finds first in its context.
	 */
	public static final String TRANSACTION_MANAGER = "transactionManager";

	private String migrations = "";
	private String entities = "";
	private String mainPackage = "";

	@Override
	public void setImportMetadata(@NotNull AnnotationMetadata metadata) {
		Map<String, Object> declared = metadata.getAnnotationAttributes(Tables.class.getName());
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(declared);
		if (attributes != null) {
			migrations = attributes.getString("migrations");
			entities = attributes.getString("entities");
		}

		mainPackage = ClassUtils.getPackageName(metadata.getClassName());
	}

	@Bean(ENTITY_MANAGER_FACTORY)
	public EntityManagerFactory pluginEntityManagerFactory(ExtensionContext extension, ApplicationContext plugin) {
		String id = extension.descriptor().getId();
		PluginDatabase database = plugin.getBeanProvider(PluginDatabase.class).getIfAvailable();
		if (database == null) throw new IllegalStateException("'" + id + "' declares @Tables, but Yui has no database");

		String folder = migrations.isBlank() ? "db/migration/" + id + "/" : withSlash(migrations);
		String packageName = entities.isBlank() ? mainPackage + ".entity" : entities;

		return database.open(id, plugin.getClassLoader(), folder, packageName);
	}

	@Bean(TRANSACTION_MANAGER)
	public JpaTransactionManager transactionManager(@Qualifier(ENTITY_MANAGER_FACTORY) EntityManagerFactory factory) {
		return new JpaTransactionManager(factory);
	}

	private static String withSlash(String folder) {
		return folder.endsWith("/") ? folder : folder + "/";
	}
}

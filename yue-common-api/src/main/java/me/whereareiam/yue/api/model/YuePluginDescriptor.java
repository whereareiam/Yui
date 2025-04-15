package me.whereareiam.yue.api.model;

import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;

import java.util.List;
import java.util.Objects;

public class YuePluginDescriptor implements PluginDescriptor {
	private String name;
	private String version;
	private List<String> authors;

	private String requires;
	private List<PluginDependency> dependencies;
	private String license;

	private String entrypoint;

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public void setEntrypoint(String entrypoint) {
		this.entrypoint = entrypoint;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getPluginId() {
		return name.replace(" ", "-").toLowerCase();
	}

	@Override
	public String getPluginDescription() {
		return "";
	}

	@Override
	public String getPluginClass() {
		return entrypoint;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getRequires() {
		return requires;
	}

	@Override
	public String getProvider() {
		return authors == null ? "" : String.join(", ", authors);
	}

	@Override
	public List<PluginDependency> getDependencies() {
		return dependencies;
	}

	@Override
	public String getLicense() {
		return license;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DefaultPluginDescriptor that)) return false;

		return Objects.equals(getPluginId(), that.getPluginId()) &&
				Objects.equals(getPluginClass(), that.getPluginClass()) &&
				Objects.equals(getVersion(), that.getVersion()) &&
				Objects.equals(getRequires(), that.getRequires()) &&
				Objects.equals(getProvider(), that.getProvider()) &&
				Objects.equals(getDependencies(), that.getDependencies()) &&
				Objects.equals(getLicense(), that.getLicense());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getPluginId(), getPluginClass(), getVersion(), getRequires(), getProvider(), getDependencies(), getLicense());
	}

	@Override
	public String toString() {
		return "YuePluginDescriptor{" +
				"name='" + name + '\'' +
				", version='" + version + '\'' +
				", providers=" + authors +
				", requires='" + requires + '\'' +
				", dependencies=" + dependencies +
				", license='" + license + '\'' +
				", pluginClass='" + entrypoint + '\'' +
				'}';
	}
}
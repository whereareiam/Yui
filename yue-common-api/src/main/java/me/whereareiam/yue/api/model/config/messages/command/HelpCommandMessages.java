package me.whereareiam.yue.api.model.config.messages.command;

import java.util.Map;

public class HelpCommandMessages {
	private String description;
	private String example;
	private Map<String, String> variables;
	private Information information;
	private Category category;

	public static class Information {
		private Global global;
		private Specific specific;

		public static class Global {
			private String title;
			private String description;

			public String getTitle() {
				return title;
			}

			public void setTitle(String title) {
				this.title = title;
			}

			public String getDescription() {
				return description;
			}

			public void setDescription(String description) {
				this.description = description;
			}
		}

		public static class Specific {
			private String title;
			private String description;
			private String headFormat;
			private String footFormat;

			public String getTitle() {
				return title;
			}

			public void setTitle(String title) {
				this.title = title;
			}

			public String getDescription() {
				return description;
			}

			public void setDescription(String description) {
				this.description = description;
			}

			public String getHeadFormat() {
				return headFormat;
			}

			public void setHeadFormat(String headFormat) {
				this.headFormat = headFormat;
			}

			public String getFootFormat() {
				return footFormat;
			}

			public void setFootFormat(String footFormat) {
				this.footFormat = footFormat;
			}
		}

		public Global getGlobal() {
			return global;
		}

		public void setGlobal(Global global) {
			this.global = global;
		}

		public Specific getSpecific() {
			return specific;
		}

		public void setSpecific(Specific specific) {
			this.specific = specific;
		}
	}

	public static class Category {
		private String utility;
		private String fun;
		private String moderation;
		private String administration;

		public String getUtility() {
			return utility;
		}

		public void setUtility(String utility) {
			this.utility = utility;
		}

		public String getFun() {
			return fun;
		}

		public void setFun(String fun) {
			this.fun = fun;
		}

		public String getModeration() {
			return moderation;
		}

		public void setModeration(String moderation) {
			this.moderation = moderation;
		}

		public String getAdministration() {
			return administration;
		}

		public void setAdministration(String administration) {
			this.administration = administration;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	public Information getInformation() {
		return information;
	}

	public void setInformation(Information information) {
		this.information = information;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
}
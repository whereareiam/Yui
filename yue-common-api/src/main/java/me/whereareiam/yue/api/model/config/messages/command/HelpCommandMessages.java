package me.whereareiam.yue.api.model.config.messages.command;

import java.util.Map;

public class HelpCommandMessages {
	private String description;
	private Map<String, String> variables;
	private Information information;
	private Category category;

	public static class Information {
		private Global global;

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

		public Global getGlobal() {
			return global;
		}

		public void setGlobal(Global global) {
			this.global = global;
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
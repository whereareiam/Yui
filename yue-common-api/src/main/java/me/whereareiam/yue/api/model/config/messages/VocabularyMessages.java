package me.whereareiam.yue.api.model.config.messages;

public class VocabularyMessages {
	private String cancel;
	private Category category;

	public static class Category {
		private String utility;
		private String fun;
		private String moderation;
		private String administration;
		private String none;

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

		public String getNone() {
			return none;
		}

		public void setNone(String none) {
			this.none = none;
		}
	}

	public String getCancel() {
		return cancel;
	}

	public void setCancel(String cancel) {
		this.cancel = cancel;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
}

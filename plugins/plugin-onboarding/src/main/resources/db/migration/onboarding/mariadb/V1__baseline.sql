-- Members who finished onboarding. Being verified outlives the role: a member who rejoins gets it back.
CREATE TABLE onboarding_members (
	user_id BIGINT NOT NULL,
	verified_at DATETIME(6) NOT NULL,
	CONSTRAINT pk_onboarding_members PRIMARY KEY (user_id)
);

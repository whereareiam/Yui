-- Members who finished onboarding. Being verified outlives the role: a member who rejoins gets it back.
CREATE TABLE onboarding_members (
	user_id BIGINT NOT NULL,
	verified_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_onboarding_members PRIMARY KEY (user_id)
);

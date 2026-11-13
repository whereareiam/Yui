-- Members who finished a verification. Being verified outlives the role: a member who rejoins gets it back.
CREATE TABLE verification_members (
	user_id BIGINT NOT NULL,
	verified_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_verification_members PRIMARY KEY (user_id)
);

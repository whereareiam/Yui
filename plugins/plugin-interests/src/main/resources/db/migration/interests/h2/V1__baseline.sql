-- The topics each member picked. Their roles follow them: a member who rejoins gets them back.
CREATE TABLE interests_picks (
	user_id BIGINT NOT NULL,
	topic VARCHAR(64) NOT NULL,
	CONSTRAINT pk_interests_picks PRIMARY KEY (user_id, topic)
);

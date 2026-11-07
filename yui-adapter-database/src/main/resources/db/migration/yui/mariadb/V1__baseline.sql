-- The tables of Yui 1.0.0. Each database has its own copy of this file, because they spell an identity column
-- differently; later migrations that read the same everywhere go into db/migration/yui/common.

CREATE TABLE yui_languages (
	id INTEGER NOT NULL AUTO_INCREMENT,
	locale VARCHAR(64) NOT NULL,
	CONSTRAINT pk_yui_languages PRIMARY KEY (id),
	CONSTRAINT uk_yui_languages_locale UNIQUE (locale)
);

CREATE TABLE yui_roles (
	id BIGINT NOT NULL,
	CONSTRAINT pk_yui_roles PRIMARY KEY (id)
);

CREATE TABLE yui_fluctlights (
	id BIGINT NOT NULL,
	language_id INTEGER,
	CONSTRAINT pk_yui_fluctlights PRIMARY KEY (id),
	CONSTRAINT fk_yui_fluctlights_language FOREIGN KEY (language_id) REFERENCES yui_languages (id)
);

CREATE TABLE yui_fluctlights_languages (
	fluctlight_id BIGINT NOT NULL,
	language_id INTEGER NOT NULL,
	CONSTRAINT pk_yui_fluctlights_languages PRIMARY KEY (fluctlight_id, language_id),
	CONSTRAINT fk_yui_fluctlights_languages_fluctlight FOREIGN KEY (fluctlight_id) REFERENCES yui_fluctlights (id),
	CONSTRAINT fk_yui_fluctlights_languages_language FOREIGN KEY (language_id) REFERENCES yui_languages (id)
);

CREATE TABLE yui_fluctlights_roles (
	fluctlight_id BIGINT NOT NULL,
	role_id BIGINT NOT NULL,
	CONSTRAINT pk_yui_fluctlights_roles PRIMARY KEY (fluctlight_id, role_id),
	CONSTRAINT fk_yui_fluctlights_roles_fluctlight FOREIGN KEY (fluctlight_id) REFERENCES yui_fluctlights (id),
	CONSTRAINT fk_yui_fluctlights_roles_role FOREIGN KEY (role_id) REFERENCES yui_roles (id)
);

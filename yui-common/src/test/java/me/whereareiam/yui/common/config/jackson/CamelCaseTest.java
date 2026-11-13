package me.whereareiam.yui.common.config.jackson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CamelCaseTest {
	@Test
	void fromConstant_WritesCamelCase() {
		assertEquals("localBuilds", CamelCase.fromConstant("LOCAL_BUILDS"));
		assertEquals("yui", CamelCase.fromConstant("YUI"));
	}

	@Test
	void toConstant_ReadsCamelCase() {
		assertEquals("LOCAL_BUILDS", CamelCase.toConstant("localBuilds"));
		assertEquals("PLUGINS", CamelCase.toConstant("plugins"));
	}
}

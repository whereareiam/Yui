package me.whereareiam.yui.adapter.database.adapter;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
		"test.database.type=H2",
		"test.database.url=jdbc:h2:mem:yui;DB_CLOSE_DELAY=-1",
		"test.database.username=sa",
		"test.database.password="
})
class AuditPersistenceAdapterH2Test extends AuditPersistenceAdapterTest {
}

package me.whereareiam.yui.adapter.database.repository;

import me.whereareiam.yui.adapter.database.entity.AuditEntryEntity;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface AuditEntryRepository extends JpaRepository<AuditEntryEntity, Long> {
	@Query("""
			select e from AuditEntryEntity e
			where e.category in :categories and e.entry like :pattern
			order by e.postedAt desc, e.id desc
			""")
	List<AuditEntryEntity> search(Collection<AuditCategory> categories, String pattern, Pageable page);

	@Modifying
	@Query("delete from AuditEntryEntity e where e.postedAt < :instant")
	int deleteBefore(Instant instant);
}

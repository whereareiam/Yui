package me.whereareiam.yui.adapter.database.repository;

import me.whereareiam.yui.adapter.database.entity.FluctlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for FluctlightEntity.
 * Provides database access for Fluctlight custom data persistence.
 */
@Repository
public interface FluctlightRepository extends JpaRepository<FluctlightEntity, Long> {
	Optional<FluctlightEntity> findById(long id);
}
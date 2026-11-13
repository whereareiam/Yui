package org.example.tables.repository;

import org.example.tables.entity.WordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<WordEntity, Long> {
}

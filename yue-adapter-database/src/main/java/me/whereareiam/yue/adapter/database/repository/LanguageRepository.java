package me.whereareiam.yue.adapter.database.repository;

import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, Integer> {
	void deleteByLocale(Locale locale);

	Optional<LanguageEntity> findByLocale(Locale locale);
}

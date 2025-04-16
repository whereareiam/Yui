package me.whereareiam.yue.adapter.database.repository;

import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, Integer> {
	void deleteByLocale(DiscordLocale locale);

	Optional<LanguageEntity> findByLocale(DiscordLocale locale);
}

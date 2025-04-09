package me.whereareiam.yue.adapter.database.repository;

import me.whereareiam.yue.adapter.database.entity.profile.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
	Optional<ProfileEntity> findById(Long id);
}

package me.whereareiam.yue.adapter.database.repository;

import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
	Optional<UserProfileEntity> findById(long id);
}

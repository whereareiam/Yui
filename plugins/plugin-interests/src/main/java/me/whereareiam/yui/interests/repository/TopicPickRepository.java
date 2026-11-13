package me.whereareiam.yui.interests.repository;

import me.whereareiam.yui.interests.entity.TopicPickEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicPickRepository extends JpaRepository<TopicPickEntity, TopicPickEntity.Key> {
	List<TopicPickEntity> findByUserId(Long userId);
}

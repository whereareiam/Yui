package me.whereareiam.yui.interests.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A topic a member picked.
 */
@Getter
@Entity
@Table(name = "interests_picks")
@IdClass(TopicPickEntity.Key.class)
@NoArgsConstructor
@AllArgsConstructor
public class TopicPickEntity {
	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(length = 64)
	private String topic;

	@Getter
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Key implements Serializable {
		private Long userId;
		private String topic;
	}
}

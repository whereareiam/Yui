package me.whereareiam.yui.onboarding.repository;

import me.whereareiam.yui.onboarding.entity.VerifiedMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerifiedMemberRepository extends JpaRepository<VerifiedMemberEntity, Long> {
}

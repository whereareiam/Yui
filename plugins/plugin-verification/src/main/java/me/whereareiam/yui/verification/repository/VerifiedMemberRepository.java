package me.whereareiam.yui.verification.repository;

import me.whereareiam.yui.verification.entity.VerifiedMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerifiedMemberRepository extends JpaRepository<VerifiedMemberEntity, Long> {
}

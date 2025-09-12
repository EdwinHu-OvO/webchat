package com.webchat.repository;

import com.webchat.domain.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByUserId(Long userId);

    List<GroupMember> findByGroupId(Long groupId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    void deleteByGroupIdAndUserId(Long groupId, Long userId);
}

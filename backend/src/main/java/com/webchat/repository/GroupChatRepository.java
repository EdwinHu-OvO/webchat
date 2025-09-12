package com.webchat.repository;

import com.webchat.domain.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    Optional<GroupChat> findByName(String name);
}

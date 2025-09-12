package com.webchat.repository;

import com.webchat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderIdAndReceiverIdOrderByCreatedAtAsc(Long senderId, Long receiverId);

    List<ChatMessage> findByGroupIdOrderByCreatedAtAsc(Long groupId);

    void deleteBySenderIdAndReceiverId(Long senderId, Long receiverId);

    void deleteByGroupId(Long groupId);
}

package com.webchat.service;

import com.webchat.domain.ChatMessage;
import com.webchat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MessageService {
    private final ChatMessageRepository chatMessageRepository;

    public MessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public List<ChatMessage> listPrivateMessages(Long userId, Long peerId) {
        List<ChatMessage> aToB = chatMessageRepository.findBySenderIdAndReceiverIdOrderByCreatedAtAsc(userId, peerId);
        List<ChatMessage> bToA = chatMessageRepository.findBySenderIdAndReceiverIdOrderByCreatedAtAsc(peerId, userId);
        List<ChatMessage> merged = new ArrayList<>();
        merged.addAll(aToB);
        merged.addAll(bToA);
        merged.sort(Comparator.comparing(ChatMessage::getCreatedAt));
        return merged;
    }

    public List<ChatMessage> listGroupMessages(Long groupId) {
        return chatMessageRepository.findByGroupIdOrderByCreatedAtAsc(groupId);
    }

    public void deletePrivateConversation(Long userId, Long peerId) {
        chatMessageRepository.deleteBySenderIdAndReceiverId(userId, peerId);
        chatMessageRepository.deleteBySenderIdAndReceiverId(peerId, userId);
    }

    public void deleteGroupMessages(Long groupId) {
        chatMessageRepository.deleteByGroupId(groupId);
    }
}

package com.webchat.web;

import com.webchat.domain.ChatMessage;
import com.webchat.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/private")
    public List<ChatMessage> listPrivate(@RequestParam Long userId, @RequestParam Long peerId) {
        return messageService.listPrivateMessages(userId, peerId);
    }

    @GetMapping("/group/{groupId}")
    public List<ChatMessage> listGroup(@PathVariable Long groupId) {
        return messageService.listGroupMessages(groupId);
    }
}

package com.webchat.socket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.webchat.domain.ChatMessage;
import com.webchat.domain.GroupChat;
import com.webchat.domain.User;
import com.webchat.repository.ChatMessageRepository;
import com.webchat.repository.GroupChatRepository;
import com.webchat.repository.UserRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSocketHandler {
    private final SocketIOServer server;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupChatRepository groupChatRepository;

    private final Map<Long, String> userIdToSession = new ConcurrentHashMap<>();

    public ChatSocketHandler(SocketIOServer server,
            ChatMessageRepository messageRepository,
            UserRepository userRepository,
            GroupChatRepository groupChatRepository) {
        this.server = server;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.groupChatRepository = groupChatRepository;
    }

    @PostConstruct
    public void init() {
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("private_message", PrivateMessage.class, onPrivateMessage());
        server.addEventListener("group_message", GroupMessage.class, onGroupMessage());
        server.addEventListener("join_group", JoinGroup.class, onJoinGroup());
    }

    private ConnectListener onConnected() {
        return client -> {
            String userIdStr = client.getHandshakeData().getSingleUrlParam("userId");
            if (userIdStr != null) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    userIdToSession.put(userId, client.getSessionId().toString());
                } catch (NumberFormatException ignored) {
                }
            }
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> userIdToSession.entrySet()
                .removeIf(e -> e.getValue().equals(client.getSessionId().toString()));
    }

    private DataListener<PrivateMessage> onPrivateMessage() {
        return (SocketIOClient client, PrivateMessage data, AckRequest ackSender) -> {
            User sender = userRepository.findById(data.senderId).orElseThrow();
            User receiver = userRepository.findById(data.receiverId).orElseThrow();

            ChatMessage msg = new ChatMessage();
            msg.setSender(sender);
            msg.setReceiver(receiver);
            msg.setContent(data.content);
            messageRepository.save(msg);

            // 仅发送给发送者与接收者
            server.getAllClients().stream()
                    .filter(c -> {
                        String uid = c.getHandshakeData().getSingleUrlParam("userId");
                        return String.valueOf(data.senderId).equals(uid) || String.valueOf(data.receiverId).equals(uid);
                    })
                    .forEach(c -> c.sendEvent("private_message", data));
        };
    }

    private DataListener<GroupMessage> onGroupMessage() {
        return (SocketIOClient client, GroupMessage data, AckRequest ackSender) -> {
            User sender = userRepository.findById(data.senderId).orElseThrow();
            GroupChat group = groupChatRepository.findById(data.groupId).orElseThrow();

            ChatMessage msg = new ChatMessage();
            msg.setSender(sender);
            msg.setGroup(group);
            msg.setContent(data.content);
            messageRepository.save(msg);

            server.getRoomOperations("group:" + data.groupId).sendEvent("group_message", data);
        };
    }

    private DataListener<JoinGroup> onJoinGroup() {
        return (client, data, ackSender) -> client.joinRoom("group:" + data.groupId);
    }

    public static class PrivateMessage {
        public Long senderId;
        public Long receiverId;
        public String content;
    }

    public static class GroupMessage {
        public Long senderId;
        public Long groupId;
        public String content;
    }

    public static class JoinGroup {
        public Long groupId;
    }
}

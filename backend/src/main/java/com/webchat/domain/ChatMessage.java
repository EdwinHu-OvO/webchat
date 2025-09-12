package com.webchat.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver; // 私聊目标，可空（群聊时为空）

    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupChat group; // 群聊目标，可空（私聊时为空）

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public GroupChat getGroup() {
        return group;
    }

    public void setGroup(GroupChat group) {
        this.group = group;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

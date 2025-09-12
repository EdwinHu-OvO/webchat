package com.webchat.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "group_members", uniqueConstraints = @UniqueConstraint(columnNames = { "group_id", "user_id" }))
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private GroupChat group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GroupChat getGroup() {
        return group;
    }

    public void setGroup(GroupChat group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

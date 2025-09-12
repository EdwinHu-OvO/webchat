package com.webchat.service;

import com.webchat.domain.GroupChat;
import com.webchat.domain.GroupMember;
import com.webchat.domain.User;
import com.webchat.repository.GroupChatRepository;
import com.webchat.repository.GroupMemberRepository;
import com.webchat.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {
    private final GroupChatRepository groupChatRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public GroupService(GroupChatRepository groupChatRepository, GroupMemberRepository groupMemberRepository,
            UserRepository userRepository, MessageService messageService) {
        this.groupChatRepository = groupChatRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
    }

    @Transactional
    public GroupChat createGroup(Long ownerId, String name) {
        groupChatRepository.findByName(name).ifPresent(g -> {
            throw new IllegalArgumentException("群名已存在");
        });
        GroupChat group = new GroupChat();
        group.setName(name);
        group.setOwnerId(ownerId);
        group = groupChatRepository.save(group);

        User owner = userRepository.findById(ownerId).orElseThrow();
        GroupMember gm = new GroupMember();
        gm.setGroup(group);
        gm.setUser(owner);
        groupMemberRepository.save(gm);
        return group;
    }

    @Transactional
    public void addMember(Long groupId, Long userId) {
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId))
            return;
        GroupChat group = groupChatRepository.findById(groupId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        GroupMember gm = new GroupMember();
        gm.setGroup(group);
        gm.setUser(user);
        groupMemberRepository.save(gm);
    }

    @Transactional
    public void joinGroupByName(Long userId, String groupName) {
        GroupChat group = groupChatRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("群聊不存在"));
        addMember(group.getId(), userId);
    }

    public java.util.List<User> listMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId).stream().map(GroupMember::getUser).toList();
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId))
            return;
        GroupChat gc = groupChatRepository.findById(groupId).orElseThrow();
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Transactional
    public void dissolveGroup(Long groupId, Long operatorId) {
        GroupChat gc = groupChatRepository.findById(groupId).orElseThrow();
        for (GroupMember gm : groupMemberRepository.findByGroupId(groupId)) {
            groupMemberRepository.delete(gm);
        }
        // 删除群消息
        messageService.deleteGroupMessages(groupId);
        groupChatRepository.delete(gc);
    }
}

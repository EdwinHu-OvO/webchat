package com.webchat.web;

import com.webchat.domain.Friendship;
import com.webchat.domain.GroupChat;
import com.webchat.service.GroupService;
import com.webchat.service.UserService;
import com.webchat.web.dto.FriendGroupDtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FriendGroupController {
    private final UserService userService;
    private final GroupService groupService;

    public FriendGroupController(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    @PostMapping("/friends")
    public void addFriend(@Valid @RequestBody FriendGroupDtos.AddFriendRequest req) {
        userService.addFriend(req.getUserId(), req.getFriendId());
    }

    @GetMapping("/users/{userId}/friends")
    public List<Friendship> listFriends(@PathVariable Long userId) {
        return userService.listFriends(userId);
    }

    @DeleteMapping("/friends")
    public void removeFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        userService.removeFriend(userId, friendId);
    }

    @PostMapping("/groups")
    public GroupChat createGroup(@Valid @RequestBody FriendGroupDtos.CreateGroupRequest req) {
        return groupService.createGroup(req.getOwnerId(), req.getName());
    }

    @PostMapping("/groups/{groupId}/members/{userId}")
    public void addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.addMember(groupId, userId);
    }

    @GetMapping("/groups/{groupId}/members")
    public List<com.webchat.domain.User> listMembers(@PathVariable Long groupId) {
        return groupService.listMembers(groupId);
    }

    @DeleteMapping("/groups/{groupId}/members/{userId}")
    public void leaveGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.leaveGroup(groupId, userId);
    }

    @DeleteMapping("/groups/{groupId}")
    public void dissolveGroup(@PathVariable Long groupId, @RequestParam Long operatorId) {
        groupService.dissolveGroup(groupId, operatorId);
    }

    @PostMapping("/groups/join")
    public void joinGroup(@Valid @RequestBody FriendGroupDtos.JoinGroupRequest req) {
        groupService.joinGroupByName(req.getUserId(), req.getName());
    }
}

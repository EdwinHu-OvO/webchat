package com.webchat.web;

import com.webchat.domain.GroupChat;
import com.webchat.domain.GroupMember;
import com.webchat.domain.User;
import com.webchat.repository.GroupMemberRepository;
import com.webchat.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UsersController {
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public UsersController(UserRepository userRepository, GroupMemberRepository groupMemberRepository) {
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @GetMapping("/search")
    public Optional<User> searchByUsername(@RequestParam("username") String username) {
        return userRepository.findByUsername(username);
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable Long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    @GetMapping("/{userId}/groups")
    public List<GroupChat> listGroups(@PathVariable Long userId) {
        List<GroupMember> members = groupMemberRepository.findByUserId(userId);
        return members.stream().map(GroupMember::getGroup).collect(Collectors.toList());
    }
}

package com.webchat.web;

import com.webchat.domain.GroupChat;
import com.webchat.domain.GroupMember;
import com.webchat.domain.User;
import com.webchat.repository.GroupMemberRepository;
import com.webchat.repository.UserRepository;
import com.webchat.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UsersController {
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;

    public UsersController(UserRepository userRepository, GroupMemberRepository groupMemberRepository,
            UserService userService) {
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userService = userService;
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

    @PostMapping(value = "/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public User uploadAvatar(@PathVariable Long userId, @RequestPart("file") MultipartFile file) {
        return userService.updateAvatar(userId, file);
    }
}

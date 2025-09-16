package com.webchat.service;

import com.webchat.domain.Friendship;
import com.webchat.domain.User;
import com.webchat.repository.FriendshipRepository;
import com.webchat.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final MessageService messageService;

    public UserService(UserRepository userRepository, FriendshipRepository friendshipRepository,
            MessageService messageService) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.messageService = messageService;
    }

    public User register(String username, String password) {
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("用户名已存在");
        });
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("密码错误");
        }
        return user;
    }

    @Transactional
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("不能添加自己为好友");
        }
        if (friendshipRepository.existsByUserIdAndFriendId(userId, friendId)) {
            return;
        }
        User user = userRepository.findById(userId).orElseThrow();
        User friend = userRepository.findById(friendId).orElseThrow();

        Friendship a = new Friendship();
        a.setUser(user);
        a.setFriend(friend);
        friendshipRepository.save(a);

        Friendship b = new Friendship();
        b.setUser(friend);
        b.setFriend(user);
        friendshipRepository.save(b);
    }

    public List<Friendship> listFriends(Long userId) {
        return friendshipRepository.findByUserId(userId);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId))
            return;
        if (!friendshipRepository.existsByUserIdAndFriendId(userId, friendId))
            return;
        friendshipRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendshipRepository.deleteByUserIdAndFriendId(friendId, userId);
        messageService.deletePrivateConversation(userId, friendId);
    }
}

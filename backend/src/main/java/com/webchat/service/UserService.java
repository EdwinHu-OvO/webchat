package com.webchat.service;

import com.webchat.domain.Friendship;
import com.webchat.domain.User;
import com.webchat.repository.FriendshipRepository;
import com.webchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final MessageService messageService;

    @Value("${webchat.uploads-dir:uploads}")
    private String uploadsDir;

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

    @Transactional
    public User updateAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        // 先校验用户是否存在，避免错误用户导致文件被保存
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        String originalName = file.getOriginalFilename();
        String ext = ".bin";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String fileName = "avatar_" + userId + "_" + timestamp + ext;

        File dir = new File(uploadsDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("无法创建上传目录");
        }

        Path target = new File(dir, fileName).toPath();
        boolean copied = false;
        // 记录旧头像路径（仅限本地 /uploads/ 文件）
        final String oldUrl = user.getAvatarUrl();
        final Path oldPathCandidate = (oldUrl != null && oldUrl.startsWith("/uploads/"))
                ? new File(uploadsDir, oldUrl.substring("/uploads/".length())).toPath()
                : null;
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            copied = true;

            String publicUrl = "/uploads/" + fileName;
            user.setAvatarUrl(publicUrl);
            User saved = userRepository.save(user);
            // 在事务提交后删除旧头像，避免回滚导致文件丢失
            if (oldPathCandidate != null && !oldPathCandidate.equals(target)) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            Files.deleteIfExists(oldPathCandidate);
                        } catch (IOException ignored) {
                        }
                    }
                });
            }
            return saved;
        } catch (Exception e) {
            // 出错时清理已写入的文件，避免孤儿文件
            if (copied) {
                try {
                    Files.deleteIfExists(target);
                } catch (IOException ignored) {
                }
            }
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("保存文件失败", e);
        }
    }
}

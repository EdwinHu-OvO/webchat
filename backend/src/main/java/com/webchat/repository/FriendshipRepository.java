package com.webchat.repository;

import com.webchat.domain.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByUserId(Long userId);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    void deleteByUserIdAndFriendId(Long userId, Long friendId);
}

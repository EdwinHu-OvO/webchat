package com.webchat.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FriendGroupDtos {
    public static class AddFriendRequest {
        @NotNull
        private Long userId;
        @NotNull
        private Long friendId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getFriendId() {
            return friendId;
        }

        public void setFriendId(Long friendId) {
            this.friendId = friendId;
        }
    }

    public static class CreateGroupRequest {
        @NotNull
        private Long ownerId;
        @NotBlank
        private String name;

        public Long getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class JoinGroupRequest {
        @NotNull
        private Long userId;
        @NotBlank
        private String name; // 通过群名加入（也可扩展为群号）

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

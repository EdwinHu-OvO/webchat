# WebChat 后端 API 文档

## 基本信息

- **基础 URL**: `http://localhost:8080`
- **数据格式**: JSON
- **编码**: UTF-8
- **CORS**: 支持跨域访问

## 认证接口

### 1. 用户注册

**接口**: `POST /api/auth/register`

**描述**: 注册新用户

**请求体**:

```json
{
  "username": "string", // 必填，用户名
  "password": "string" // 必填，密码
}
```

**响应**:

```json
{
  "id": 1,
  "username": "testuser",
  "password": "password123"
}
```

**错误响应**:

- `400 Bad Request`: 用户名已存在
- `400 Bad Request`: 参数验证失败

### 2. 用户登录

**接口**: `POST /api/auth/login`

**描述**: 用户登录验证

**请求体**:

```json
{
  "username": "string", // 必填，用户名
  "password": "string" // 必填，密码
}
```

**响应**:

```json
{
  "id": 1,
  "username": "testuser",
  "password": "password123"
}
```

**错误响应**:

- `400 Bad Request`: 用户不存在
- `400 Bad Request`: 密码错误

## 用户管理接口

### 1. 搜索用户

**接口**: `GET /api/users/search`

**描述**: 根据用户名搜索用户

**查询参数**:

- `username` (string, 必填): 要搜索的用户名

**请求示例**: `GET /api/users/search?username=testuser`

**响应**:

```json
{
  "id": 1,
  "username": "testuser",
  "password": "password123"
}
```

**错误响应**:

- `404 Not Found`: 用户不存在时返回空

### 2. 根据 ID 获取用户

**接口**: `GET /api/users/{userId}`

**描述**: 根据用户 ID 获取用户信息

**路径参数**:

- `userId` (long, 必填): 用户 ID

**响应**:

```json
{
  "id": 1,
  "username": "testuser",
  "password": "password123"
}
```

**错误响应**:

- `404 Not Found`: 用户不存在

### 3. 获取用户群组列表

**接口**: `GET /api/users/{userId}/groups`

**描述**: 获取用户加入的所有群组

**路径参数**:

- `userId` (long, 必填): 用户 ID

**响应**:

```json
[
  {
    "id": 1,
    "name": "技术交流群",
    "ownerId": 2
  },
  {
    "id": 2,
    "name": "项目讨论组",
    "ownerId": 1
  }
]
```

## 好友管理接口

### 1. 添加好友

**接口**: `POST /api/friends`

**描述**: 添加好友关系（双向）

**请求体**:

```json
{
  "userId": 1, // 必填，当前用户ID
  "friendId": 2 // 必填，要添加的好友ID
}
```

**响应**: `200 OK` (无响应体)

**错误响应**:

- `400 Bad Request`: 不能添加自己为好友
- `404 Not Found`: 用户不存在

### 2. 获取好友列表

**接口**: `GET /api/users/{userId}/friends`

**描述**: 获取用户的好友列表

**路径参数**:

- `userId` (long, 必填): 用户 ID

**响应**:

```json
[
  {
    "id": 1,
    "user": {
      "id": 1,
      "username": "user1",
      "password": "pass1"
    },
    "friend": {
      "id": 2,
      "username": "user2",
      "password": "pass2"
    }
  }
]
```

### 3. 删除好友

**接口**: `DELETE /api/friends`

**描述**: 删除好友关系（双向）并删除聊天记录

**查询参数**:

- `userId` (long, 必填): 当前用户 ID
- `friendId` (long, 必填): 要删除的好友 ID

**请求示例**: `DELETE /api/friends?userId=1&friendId=2`

**响应**: `200 OK` (无响应体)

## 群组管理接口

### 1. 创建群组

**接口**: `POST /api/groups`

**描述**: 创建新群组，创建者自动成为群主和成员

**请求体**:

```json
{
  "ownerId": 1, // 必填，群主用户ID
  "name": "技术交流群" // 必填，群组名称（唯一）
}
```

**响应**:

```json
{
  "id": 1,
  "name": "技术交流群",
  "ownerId": 1
}
```

**错误响应**:

- `400 Bad Request`: 群名已存在

### 2. 邀请加入群组

**接口**: `POST /api/groups/{groupId}/members/{userId}`

**描述**: 邀请用户加入群组

**路径参数**:

- `groupId` (long, 必填): 群组 ID
- `userId` (long, 必填): 被邀请用户 ID

**响应**: `200 OK` (无响应体)

**错误响应**:

- `404 Not Found`: 群组或用户不存在

### 3. 通过群名加入群组

**接口**: `POST /api/groups/join`

**描述**: 用户主动通过群名加入群组

**请求体**:

```json
{
  "userId": 1, // 必填，用户ID
  "name": "技术交流群" // 必填，群组名称
}
```

**响应**: `200 OK` (无响应体)

**错误响应**:

- `400 Bad Request`: 群聊不存在

### 4. 获取群组成员

**接口**: `GET /api/groups/{groupId}/members`

**描述**: 获取群组所有成员列表

**路径参数**:

- `groupId` (long, 必填): 群组 ID

**响应**:

```json
[
  {
    "id": 1,
    "username": "user1",
    "password": "pass1"
  },
  {
    "id": 2,
    "username": "user2",
    "password": "pass2"
  }
]
```

### 5. 退出群组

**接口**: `DELETE /api/groups/{groupId}/members/{userId}`

**描述**: 用户退出群组

**路径参数**:

- `groupId` (long, 必填): 群组 ID
- `userId` (long, 必填): 用户 ID

**响应**: `200 OK` (无响应体)

### 6. 解散群组

**接口**: `DELETE /api/groups/{groupId}`

**描述**: 群主解散群组，删除所有成员和消息记录

**路径参数**:

- `groupId` (long, 必填): 群组 ID

**查询参数**:

- `operatorId` (long, 必填): 操作者 ID（需为群主）

**请求示例**: `DELETE /api/groups/1?operatorId=1`

**响应**: `200 OK` (无响应体)

**错误响应**:

- `404 Not Found`: 群组不存在

## 消息接口

### 1. 获取私聊消息历史

**接口**: `GET /api/messages/private`

**描述**: 获取两个用户之间的私聊消息历史

**查询参数**:

- `userId` (long, 必填): 当前用户 ID
- `peerId` (long, 必填): 聊天对象 ID

**请求示例**: `GET /api/messages/private?userId=1&peerId=2`

**响应**:

```json
[
  {
    "id": 1,
    "sender": {
      "id": 1,
      "username": "user1",
      "password": "pass1"
    },
    "receiver": {
      "id": 2,
      "username": "user2",
      "password": "pass2"
    },
    "group": null,
    "content": "你好！",
    "createdAt": "2025-01-01T10:00:00Z"
  },
  {
    "id": 2,
    "sender": {
      "id": 2,
      "username": "user2",
      "password": "pass2"
    },
    "receiver": {
      "id": 1,
      "username": "user1",
      "password": "pass1"
    },
    "group": null,
    "content": "你好，很高兴认识你！",
    "createdAt": "2025-01-01T10:01:00Z"
  }
]
```

### 2. 获取群聊消息历史

**接口**: `GET /api/messages/group/{groupId}`

**描述**: 获取群组的消息历史

**路径参数**:

- `groupId` (long, 必填): 群组 ID

**响应**:

```json
[
  {
    "id": 3,
    "sender": {
      "id": 1,
      "username": "user1",
      "password": "pass1"
    },
    "receiver": null,
    "group": {
      "id": 1,
      "name": "技术交流群",
      "ownerId": 1
    },
    "content": "大家好！",
    "createdAt": "2025-01-01T10:05:00Z"
  }
]
```

## WebSocket 事件

### 连接配置

- **WebSocket 服务器**: `http://localhost:9092`
- **连接参数**: `?userId={用户ID}`
- **传输方式**: `websocket`

### 事件类型

#### 1. 私聊消息

**事件名**: `private_message`

**发送数据**:

```json
{
  "senderId": 1, // 发送者ID
  "receiverId": 2, // 接收者ID
  "content": "消息内容"
}
```

**接收数据**: 同发送数据

#### 2. 群聊消息

**事件名**: `group_message`

**发送数据**:

```json
{
  "senderId": 1, // 发送者ID
  "groupId": 1, // 群组ID
  "content": "消息内容"
}
```

**接收数据**: 同发送数据

#### 3. 加入群聊房间

**事件名**: `join_group`

**发送数据**:

```json
{
  "groupId": 1 // 群组ID
}
```

## 数据模型

### User (用户)

```json
{
  "id": "long", // 用户ID
  "username": "string", // 用户名（唯一）
  "password": "string" // 密码
}
```

### GroupChat (群组)

```json
{
  "id": "long", // 群组ID
  "name": "string", // 群组名称（唯一）
  "ownerId": "long" // 群主用户ID
}
```

### ChatMessage (消息)

```json
{
  "id": "long", // 消息ID
  "sender": "User", // 发送者
  "receiver": "User", // 接收者（私聊，可为null）
  "group": "GroupChat", // 群组（群聊，可为null）
  "content": "string", // 消息内容
  "createdAt": "instant" // 创建时间
}
```

### Friendship (好友关系)

```json
{
  "id": "long", // 关系ID
  "user": "User", // 用户
  "friend": "User" // 好友
}
```

## 错误处理

### 通用错误格式

```json
{
  "message": "错误描述信息"
}
```

### 常见 HTTP 状态码

- `200 OK`: 请求成功
- `400 Bad Request`: 请求参数错误或业务逻辑错误
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器内部错误

## 业务规则

1. **好友关系**: 添加好友时会创建双向关系，删除时也会同时删除双向关系
2. **群组管理**: 只有群主可以解散群组，成员可以主动退出
3. **消息存储**: 私聊和群聊消息都会持久化存储
4. **用户名唯一**: 注册时用户名必须唯一，群组名称也必须唯一
5. **自动清理**: 删除好友关系时会自动删除相关私聊记录，解散群组时会删除群聊记录

## 开发建议

1. 建议在生产环境中对密码进行加密存储
2. 建议添加 JWT 认证机制保护 API 安全
3. 建议添加请求频率限制防止滥用
4. 建议添加详细的日志记录便于问题排查
5. 建议对敏感操作添加权限验证

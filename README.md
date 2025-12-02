# WebChat 实时聊天系统

## 可用于作业交差

## 面向大模型开发 ~~（AI 写的）~~

也可使用 react 重构的前端https://github.com/EdwinHu-OvO/webchat-frontend-next

## 简要说明：

- 用户可注册、登录，查看用户列表/好友列表。
- 用户可添加好友、创建或加入群聊。
- 用户可进行私聊与群聊，消息经由 REST 获取历史、Socket.IO 实时分发。
- 前端使用 Vue 3 + Vite，页面包含登录/注册与聊天界面，使用 Pinia 管理用户状态。
- 后端为 Spring Boot，暴露认证、用户、好友/群组与消息 REST 接口，并通过 Socket.IO 处理实时聊天事件。
- 数据层采用 JPA 访问 SQLite 本地数据库，存储用户、好友关系、群聊、消息等信息。

## 运行方式

### 环境要求

- Java 17+
- Node.js 16+
- npm 或 yarn

### 后端启动

1. 进入后端目录：

   ```bash
   cd backend
   ```

2. 安装依赖并启动：

   ```bash
   ./mvnw spring-boot:run
   ```

   或使用 Maven：

   ```bash
   mvn spring-boot:run
   ```

3. 后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. 进入前端目录：

   ```bash
   cd frontend
   ```

2. 安装依赖：

   ```bash
   npm install
   ```

3. 启动开发服务器：

   ```bash
   npm run dev
   ```

4. 前端应用将在 `http://localhost:5173` 启动

### 访问应用

- 打开浏览器访问 `http://localhost:5173`
- 注册新用户或使用现有账户登录
- 开始聊天体验

### 数据库

- 使用 SQLite 数据库，数据文件位于 `backend/data/webchat.db`
- 首次运行时会自动创建数据库表和初始数据

### 用户头像

- 上传文件位于 `backend/data/uplodes`

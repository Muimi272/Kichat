# Kichat

Kichat 是一个轻量级实时聊天室，基于 Spring Boot、Thymeleaf、WebSocket 和 STOMP 构建。

项目提供：

- 基于用户名的加入流程
- 公共聊天室聊天
- 在线用户之间的私聊
- 在线用户查询，用于收件人建议
- 加入/离开的系统消息
- 支持亮色/暗色主题切换的前端页面

这个仓库是一个小型全栈演示项目：后端是 Spring Boot WebSocket 应用，前端使用 Thymeleaf 模板渲染，并在浏览器端使用 STOMP 逻辑。

## 技术栈

- Java 25
- Spring Boot 4.0.6
- Spring MVC
- Spring WebSocket + STOMP
- Thymeleaf
- Lombok
- Maven / Maven Wrapper
- 原生 HTML、CSS 和 JavaScript

## 功能

- 唯一用户名加入流程
  用户从 `/join` 页面加入聊天室。重复用户名会被服务器拒绝。

- 公共聊天
  发送到公共聊天室的消息会广播给所有已连接用户。

- 私聊
  用户可以向另一个在线用户发送私信。

- 基于提及的收件人选择
  在聊天室页面输入 `@username` 会触发收件人建议，并将输入区切换到私聊模式。

- 在线用户查询
  前端会向后端请求当前在线用户列表，用于支持私聊。

- 感知会话的身份校验
  后端会把用户名绑定到 WebSocket 会话，并在处理聊天操作前根据会话校验消息归属。

- 主题切换
  UI 支持亮色模式和暗色模式，所选主题会存储在 `localStorage` 中。

## 项目结构

```text
Kichat/
├─ .mvn/
│  └─ wrapper/
│     └─ maven-wrapper.properties
├─ .gitattributes
├─ src/
│  ├─ main/
│  │  ├─ java/com/muimi/kichat/
│  │  │  ├─ controller/
│  │  │  │  ├─ ChatController.java
│  │  │  │  ├─ PageController.java
│  │  │  │  └─ WebSocketConfig.java
│  │  │  ├─ entity/
│  │  │  │  ├─ ChatMessage.java
│  │  │  │  └─ Type.java
│  │  │  ├─ repo/
│  │  │  │  └─ UserRepo.java
│  │  │  ├─ Service/
│  │  │  │  └─ UserService.java
│  │  │  ├─ util/
│  │  │  │  └─ Kitimer.java
│  │  │  └─ KichatApplication.java
│  │  └─ resources/
│  │     ├─ application.properties
│  │     └─ templates/
│  │        ├─ error.html
│  │        ├─ join.html
│  │        └─ room.html
├─ README.md
├─ mvnw
├─ mvnw.cmd
└─ pom.xml
```

## 工作原理

### 页面路由

- `GET /join`
  渲染加入页面。

- `GET /room`
  渲染聊天室页面。

当前没有为 `/` 配置控制器映射，因此推荐入口地址是：

```text
http://localhost:8080/join
```

### WebSocket 端点

STOMP 端点是：

```text
/ws-chat
```

前端会直接连接到：

- `ws://<host>/ws-chat`
- 通过 HTTPS 提供服务时使用 `wss://<host>/ws-chat`

### STOMP 配置

应用目的地址前缀是：

```text
/Kichat
```

消息代理前缀是：

```text
/topic
/queue
```

用户目的地址前缀是：

```text
/user
```

心跳已启用：

```text
30s send / 30s receive
```

## 消息路由

### 客户端 -> 服务端

- `/Kichat/join`
  加入聊天室

- `/Kichat/chat`
  发送公共消息

- `/Kichat/private`
  发送私聊消息

- `/Kichat/leave`
  离开聊天室

- `/Kichat/queryAll`
  查询当前在线用户列表

### 服务端 -> 客户端

- `/topic/public`
  广播公共消息以及加入/离开事件

- `/user/queue/private`
  向指定用户会话投递私聊消息

- `/user/queue/error`
  将校验错误或身份错误返回给当前用户会话

- `/user/queue/queryAll`
  将当前在线用户列表返回给发起请求的会话

## 消息模型

后端使用 `ChatMessage`，字段如下：

- `type`
- `sender`
- `receiver`
- `content`
- `time`

支持的消息类型：

- `JOIN`
- `LEAVE`
- `CHAT`
- `ERROR`
- `PRIVATE`
- `QUERY`

## 用户和会话管理

在线用户通过 `UserRepo` 内部的并发集合存储在内存中。

后端保存的内容：

- 一个用户名并发集合
- 一个 username -> sessionId 映射

这在实际使用中意味着：

- 用户状态不会持久化
- 应用重启后会清空所有用户
- 适合作为演示或小型本地项目，不适合生产级横向扩展

## 前端行为

### join.html

加入页面会：

- 连接到 WebSocket 端点
- 让用户输入用户名
- 向后端发送 `JOIN` 消息
- 将用户名存入 `sessionStorage`
- 加入成功后跳转到 `/room`

### room.html

聊天室页面会：

- 要求从 `sessionStorage` 中读取用户名
- 连接到同一个 WebSocket 端点
- 显示公共消息、私聊消息和系统消息
- 支持通过 `@username` 指定私聊对象
- 向服务器查询在线用户
- 允许从私聊模式切回公共模式
- 支持带确认的离开聊天室流程
- 支持亮色/暗色主题切换

## 运行项目

### 环境要求

- JDK 25
- Maven 3.9+ 或仓库内置的 Maven Wrapper

### 开发环境启动

在 Windows 上使用 Maven Wrapper：

```powershell
.\mvnw.cmd spring-boot:run
```

在 macOS/Linux 上使用 Maven Wrapper：

```bash
./mvnw spring-boot:run
```

使用本地安装的 Maven：

```bash
mvn spring-boot:run
```

然后打开：

```text
http://localhost:8080/join
```

### 构建项目

```bash
mvn clean package
```

或者：

```powershell
.\mvnw.cmd clean package
```

## 测试

当前测试套件包含一个基础的 Spring 上下文启动测试：

```bash
mvn test
```

或者：

```powershell
.\mvnw.cmd test
```

## 说明和限制

- 用户数据只保存在内存中
- 没有数据库集成
- 除了基于会话绑定的用户名校验外，没有额外认证机制
- `/` 未映射，请使用 `/join`
- 前端依赖从 jsDelivr CDN 加载的 STOMP 客户端
- 当前自动化测试较少

## 故障排查

### Maven Wrapper 无法启动

如果 wrapper 脚本在你的环境中失败，请改用本地安装的 Maven：

```bash
mvn spring-boot:run
```

### 无法进入聊天室

请检查以下内容：

- 服务器正在运行
- 你打开的是 `/join`，而不是直接打开 `/room`
- 用户名没有被占用
- 浏览器可以访问 `/ws-chat`

### 私聊没有显示收件人

私聊依赖在线用户查询流程。请确认：

- 目标用户已经在线
- WebSocket 连接处于活动状态
- 当前用户已经成功加入聊天室

## 主要后端类

- [KichatApplication](src/main/java/com/muimi/kichat/KichatApplication.java)
  Spring Boot 应用入口

- [WebSocketConfig](src/main/java/com/muimi/kichat/controller/WebSocketConfig.java)
  WebSocket/STOMP 端点、消息代理和心跳配置

- [ChatController](src/main/java/com/muimi/kichat/controller/ChatController.java)
  主要 WebSocket 消息处理逻辑

- [PageController](src/main/java/com/muimi/kichat/controller/PageController.java)
  Thymeleaf 页面路由

- [UserRepo](src/main/java/com/muimi/kichat/repo/UserRepo.java)
  内存中的用户和会话存储

- [UserService](src/main/java/com/muimi/kichat/Service/UserService.java)
  仓库之上的轻量服务层

- [ChatMessage](src/main/java/com/muimi/kichat/entity/ChatMessage.java)
  消息载荷模型

- [Type](src/main/java/com/muimi/kichat/entity/Type.java)
  支持的消息类型

- [Kitimer](src/main/java/com/muimi/kichat/util/Kitimer.java)
  后端使用的缓存版 `HH:mm:ss` 时间格式化工具

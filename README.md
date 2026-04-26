# Kichat

Kichat 是一个基于 Spring Boot、Thymeleaf、WebSocket 和 STOMP 的轻量级实时聊天室。

当前版本使用单页聊天室界面：用户进入同一个页面后先完成加入，再在同一条 WebSocket 会话中进行公共聊天、私聊和在线用户查询，避免了页面跳转导致的连接丢失问题。

## 技术栈

- Java 25
- Spring Boot 4.0.6
- Spring MVC
- Spring WebSocket + STOMP
- Thymeleaf
- Lombok
- Maven / Maven Wrapper
- 原生 HTML、CSS 和 JavaScript

## 当前功能

- 单页加入与聊天流程
- 唯一用户名加入校验
- 公共聊天室消息广播
- 在线用户之间的私聊
- `@username` 提及触发的私聊目标建议
- 在线用户列表查询
- 基于 WebSocket 会话的身份校验
- 用户断开连接后的自动离线广播
- 亮色 / 暗色主题切换

## 项目结构

```text
Kichat/
├─ .mvn/
│  └─ wrapper/
│     └─ maven-wrapper.properties
├─ src/
│  ├─ main/
│  │  ├─ java/com/muimi/kichat/
│  │  │  ├─ controller/
│  │  │  │  ├─ ChatController.java
│  │  │  │  ├─ PageController.java
│  │  │  │  ├─ WebSocketConfig.java
│  │  │  │  └─ WebSocketEventListener.java
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
├─ .gitattributes
├─ pom.xml
├─ mvnw
├─ mvnw.cmd
└─ README.md
```

说明：

- 当前页面路由实际使用的是 `room.html`
- `join.html` 仍保留在仓库中，但当前 `PageController` 没有把它作为入口页面暴露出来

## 页面路由

当前 HTTP 页面入口如下：

- `GET /`
  渲染聊天室单页界面

- `GET /kichat`
  渲染同一个聊天室单页界面

推荐直接访问：

```text
http://localhost:8080/
```

## WebSocket 配置

### STOMP 端点

```text
/ws-chat
```

浏览器会根据当前协议连接到：

- `ws://<host>/ws-chat`
- `wss://<host>/ws-chat`

### 应用目的地址前缀

```text
/Kichat
```

### 消息代理前缀

```text
/topic
/queue
```

### 用户目的地址前缀

```text
/user
```

### 心跳

当前启用的心跳配置为：

```text
10s send / 10s receive
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
  广播公共消息，以及加入 / 离开系统消息

- `/user/queue/private`
  投递私聊消息给指定用户会话

- `/user/queue/error`
  把校验失败、身份失败或业务错误返回给当前会话

- `/user/queue/queryAll`
  把在线用户列表返回给发起查询的会话

## 消息模型

后端消息载荷类为 `ChatMessage`，字段如下：

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

## 当前前端行为

当前活跃页面模板是 `room.html`，它承担了加入和聊天两部分职责：

- 页面加载后先建立 WebSocket / STOMP 连接
- 用户在同一页输入用户名并发送 `JOIN`
- 加入成功后切换到聊天界面，不再发生页面跳转
- 公共消息显示在主消息流中
- 输入 `@username` 时会请求在线用户并展示私聊候选
- 命中候选后输入框进入私聊模式
- 点击离开时发送 `LEAVE`，随后回到同页的加入状态
- 主题选择保存在 `localStorage`
- 最近一次加入成功的用户名会保存在 `sessionStorage`，用于回填输入框

## 用户与会话管理

在线用户信息由 `UserRepo` 使用内存并发集合维护。

当前实现包含两类映射：

- `username -> sessionId`
- `sessionId -> username`

这带来以下特性：

- 用户状态不会持久化
- 应用重启后在线列表会被清空
- 同名用户不能重复加入
- 后端会按 WebSocket 会话校验消息发送者身份
- 连接断开时，`WebSocketEventListener` 会移除用户并广播离开消息

## 运行项目

### 环境要求

- JDK 25
- Maven 3.9+，或仓库内置的 Maven Wrapper

### 启动方式

Windows：

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux：

```bash
./mvnw spring-boot:run
```

本地 Maven：

```bash
mvn spring-boot:run
```

启动后访问：

```text
http://localhost:8080/
```

也可以访问：

```text
http://localhost:8080/kichat
```

### 构建

使用本地 Maven：

```bash
mvn clean package
```

使用 Maven Wrapper：

```powershell
.\mvnw.cmd clean package
```

## 说明与限制

- 用户数据只保存在内存中
- 没有数据库集成
- 没有独立认证体系，当前主要依赖会话绑定校验
- 前端 STOMP 客户端通过 jsDelivr CDN 加载
- 当前仓库中仍保留 `join.html`，但实际入口页面已经统一为 `room.html`

## 故障排查

### Maven Wrapper 无法启动

如果你的环境里 `mvnw` 或 `mvnw.cmd` 无法正常启动，可以改用本地安装的 Maven：

```bash
mvn spring-boot:run
```

### 无法加入聊天室

请检查以下内容：

- 服务端是否已经启动
- 浏览器访问的是否是 `/` 或 `/kichat`
- 用户名是否已被其他在线用户占用
- 浏览器是否可以建立到 `/ws-chat` 的 WebSocket 连接

### 私聊候选没有出现

请检查以下内容：

- 当前用户是否已经成功加入聊天室
- WebSocket 连接是否处于活动状态
- 目标用户是否在线
- 输入内容是否以 `@` 开头触发提及逻辑

## 主要后端类

- [KichatApplication](src/main/java/com/muimi/kichat/KichatApplication.java)
  Spring Boot 应用入口

- [WebSocketConfig](src/main/java/com/muimi/kichat/controller/WebSocketConfig.java)
  WebSocket 端点、消息代理和心跳配置

- [ChatController](src/main/java/com/muimi/kichat/controller/ChatController.java)
  STOMP 消息处理逻辑

- [WebSocketEventListener](src/main/java/com/muimi/kichat/controller/WebSocketEventListener.java)
  监听断开连接事件并清理在线用户

- [PageController](src/main/java/com/muimi/kichat/controller/PageController.java)
  页面路由定义

- [UserRepo](src/main/java/com/muimi/kichat/repo/UserRepo.java)
  内存中的用户 / 会话映射存储

- [UserService](src/main/java/com/muimi/kichat/Service/UserService.java)
  对用户存储的轻量封装

- [ChatMessage](src/main/java/com/muimi/kichat/entity/ChatMessage.java)
  消息载荷模型

- [Type](src/main/java/com/muimi/kichat/entity/Type.java)
  支持的消息类型枚举

- [Kitimer](src/main/java/com/muimi/kichat/util/Kitimer.java)
  服务端使用的 `HH:mm:ss` 时间格式化工具

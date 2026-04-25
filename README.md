# Kichat

Kichat is a lightweight real-time chat room built with Spring Boot, Thymeleaf, WebSocket, and STOMP.

The project provides:

- A username-based join flow
- Public room chat
- Private chat between online users
- Online user lookup for recipient suggestions
- Join/leave system messages
- A frontend with light/dark theme switching

This repository is a small full-stack demo: the backend is a Spring Boot WebSocket application, and the frontend is rendered with Thymeleaf templates plus browser-side STOMP logic.

## Tech Stack

- Java 25
- Spring Boot 4.0.6
- Spring MVC
- Spring WebSocket + STOMP
- Thymeleaf
- Lombok
- Maven / Maven Wrapper
- Plain HTML, CSS, and JavaScript

## Features

- Unique username join flow
  Users join the room from `/join`. Duplicate usernames are rejected by the server.

- Public chat
  Messages sent to the public room are broadcast to all connected users.

- Private chat
  A user can send direct messages to another online user.

- Mention-based recipient selection
  On the room page, typing `@username` triggers recipient suggestions and switches the composer into private mode.

- Online user query
  The frontend requests the current online user list from the backend to support private messaging.

- Session-aware identity verification
  The backend binds usernames to WebSocket sessions and verifies message ownership against the session before processing chat operations.

- Theme switching
  The UI supports light mode and dark mode, with the selected theme stored in `localStorage`.

## Project Structure

```text
Kichat/
├─ .mvn/
│  └─ wrapper/
│     └─ maven-wrapper.properties
├─ .gitattributes
├─ .gitignore
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
│  │        ├─ join.html
│  │        └─ room.html
│  └─ test/
│     └─ java/com/muimi/kichat/KichatApplicationTests.java
├─ README.md
├─ mvnw
├─ mvnw.cmd
└─ pom.xml
```

## How It Works

### Page Routing

- `GET /join`
  Renders the join page.

- `GET /room`
  Renders the chat room page.

There is currently no controller mapping for `/`, so the intended entry URL is:

```text
http://localhost:8080/join
```

### WebSocket Endpoint

The STOMP endpoint is:

```text
/ws-chat
```

The frontend connects directly to:

- `ws://<host>/ws-chat`
- `wss://<host>/ws-chat` when served over HTTPS

### STOMP Configuration

The application destination prefix is:

```text
/Kichat
```

The broker prefixes are:

```text
/topic
/queue
```

The user destination prefix is:

```text
/user
```

Heartbeat is enabled with:

```text
30s send / 30s receive
```

## Message Routes

### Client -> Server

- `/Kichat/join`
  Join the room

- `/Kichat/chat`
  Send a public message

- `/Kichat/private`
  Send a private message

- `/Kichat/leave`
  Leave the room

- `/Kichat/queryAll`
  Query the current online user list

### Server -> Client

- `/topic/public`
  Broadcast public messages and join/leave events

- `/user/queue/private`
  Deliver private messages to a specific user session

- `/user/queue/error`
  Deliver validation or identity errors back to the current user session

- `/user/queue/queryAll`
  Return the current online user list to the requesting session

## Message Model

The backend uses `ChatMessage` with these fields:

- `type`
- `sender`
- `receiver`
- `content`
- `time`

Supported message types:

- `JOIN`
- `LEAVE`
- `CHAT`
- `ERROR`
- `PRIVATE`
- `QUERY`

## User and Session Management

Online users are stored in-memory using concurrent collections inside `UserRepo`.

What the backend keeps:

- A concurrent set of usernames
- A username -> sessionId map

What this means in practice:

- User state is not persisted
- Restarting the application clears all users
- This is suitable for a demo or small local project, not for production-grade horizontal scaling

## Frontend Behavior

### join.html

The join page:

- Connects to the WebSocket endpoint
- Lets the user enter a username
- Sends a `JOIN` message to the backend
- Stores the username in `sessionStorage`
- Redirects to `/room` after successful join

### room.html

The room page:

- Requires a username from `sessionStorage`
- Connects to the same WebSocket endpoint
- Displays public, private, and system messages
- Supports private targeting via `@username`
- Queries the server for online users
- Allows switching back from private mode to public mode
- Supports leave-room flow with confirmation
- Supports theme switching between light and dark mode

## Running the Project

### Requirements

- JDK 25
- Maven 3.9+ or the included Maven Wrapper

### Start in Development

Using Maven Wrapper on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Using Maven Wrapper on macOS/Linux:

```bash
./mvnw spring-boot:run
```

Using a local Maven installation:

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080/join
```

### Build the Project

```bash
mvn clean package
```

Or:

```powershell
.\mvnw.cmd clean package
```

## Testing

The test suite currently contains a basic Spring context startup test:

```bash
mvn test
```

Or:

```powershell
.\mvnw.cmd test
```

## Notes and Limitations

- User data is in-memory only
- There is no database integration
- There is no authentication beyond session-bound username validation
- `/` is not mapped; use `/join`
- The frontend depends on the STOMP client loaded from jsDelivr CDN
- The current automated tests are minimal

## Troubleshooting

### Maven Wrapper does not start

If the wrapper script fails in your environment, use a locally installed Maven instead:

```bash
mvn spring-boot:run
```

### Cannot enter the room

Check the following:

- The server is running
- You opened `/join`, not `/room` directly
- The username is not already in use
- The browser can reach `/ws-chat`

### Private messaging does not show a recipient

Private messaging depends on the online user query flow. Make sure:

- The target user is already online
- The WebSocket connection is active
- The current user successfully joined before entering the room

## Main Backend Classes

- [KichatApplication](src/main/java/com/muimi/kichat/KichatApplication.java)
  Spring Boot application entry point

- [WebSocketConfig](src/main/java/com/muimi/kichat/controller/WebSocketConfig.java)
  WebSocket/STOMP endpoint, broker, and heartbeat configuration

- [ChatController](src/main/java/com/muimi/kichat/controller/ChatController.java)
  Main WebSocket message handling logic

- [PageController](src/main/java/com/muimi/kichat/controller/PageController.java)
  Thymeleaf page routing

- [UserRepo](src/main/java/com/muimi/kichat/repo/UserRepo.java)
  In-memory user and session storage

- [UserService](src/main/java/com/muimi/kichat/Service/UserService.java)
  Thin service layer over the repository

- [ChatMessage](src/main/java/com/muimi/kichat/entity/ChatMessage.java)
  Message payload model

- [Type](src/main/java/com/muimi/kichat/entity/Type.java)
  Supported message types

- [Kitimer](src/main/java/com/muimi/kichat/util/Kitimer.java)
  Cached `HH:mm:ss` time formatter used by the backend

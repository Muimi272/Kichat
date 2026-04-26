package com.muimi.kichat.controller;

import com.muimi.kichat.Service.UserService;
import com.muimi.kichat.entity.ChatMessage;
import com.muimi.kichat.entity.Type;
import club.muimi.Kitimer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public ChatController(SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @MessageMapping("/join")
    public void join(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        if (basicNotNullVerify(chatMessage)) return;
        if (!chatMessage.getType().equals(Type.JOIN)) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("Type error");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return;
        }
        if (chatMessage.getSender().contains("[") || chatMessage.getSender().contains("]")) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("Illegal username");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return;
        }
        if (userService.addUser(chatMessage.getSender(), headerAccessor.getSessionId())) {
            if (headerAccessor.getSessionAttributes() != null) {
                headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
            }
            chatMessage.setTime(Kitimer.getCurrentTime());
            chatMessage.setContent(chatMessage.getSender() + " joined room.");
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        } else {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("User already exists.");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
        }
    }

    @MessageMapping("/chat")
    public void chat(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        if (verify(chatMessage, headerAccessor)) return;
        if (!chatMessage.getType().equals(Type.CHAT)) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("Type error");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return;
        }
        if (!userService.containsUser(chatMessage.getSender())) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("User does not exist.");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return;
        }
        if (contentNotNullVerify(chatMessage, headerAccessor)) return;
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }

    private boolean contentNotNullVerify(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("Do not send empty messages");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return true;
        }
        chatMessage.setTime(Kitimer.getCurrentTime());
        return false;
    }

    @MessageMapping("/private")
    public void privateChat(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        if (verify(chatMessage, headerAccessor)) return;
        if (!chatMessage.getType().equals(Type.PRIVATE)) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("Type error");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return;
        }
        if (!userService.containsUser(chatMessage.getSender()) || !userService.containsUser(chatMessage.getReceiver())) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("User does not exist.");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return;
        }
        if (contentNotNullVerify(chatMessage, headerAccessor)) return;
        sendPrivateToUserSession(chatMessage.getSender(), chatMessage);
        if (!chatMessage.getSender().equals(chatMessage.getReceiver())) {
            sendPrivateToUserSession(chatMessage.getReceiver(), chatMessage);
        }
    }

    @MessageMapping("/leave")
    public void leave(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        if (verify(chatMessage, headerAccessor)) return;
        if (!chatMessage.getType().equals(Type.LEAVE)) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("Type error");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return;
        }
        if (!userService.removeUser(chatMessage.getSender(), headerAccessor.getSessionId())) {
            if (userService.containsUser(chatMessage.getSender())) {
                chatMessage.setType(Type.ERROR);
                chatMessage.setContent("User deletion failed.");
                chatMessage.setTime(Kitimer.getCurrentTime());
                sendErrorToSession(headerAccessor, chatMessage);
                return;
            }
        }
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().remove("username");
        }
        chatMessage.setContent(chatMessage.getSender() + " left room.");
        chatMessage.setTime(Kitimer.getCurrentTime());
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }

    private boolean verify(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        if (basicNotNullVerify(chatMessage)) return true;
        if (!userIdentityVerify(chatMessage, headerAccessor)) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("User identity verification failed.");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return true;
        }
        return false;
    }

    private boolean basicNotNullVerify(ChatMessage chatMessage) {
        return chatMessage == null || chatMessage.getType() == null || chatMessage.getSender() == null;
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    private boolean userIdentityVerify(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String sender = chatMessage.getSender();
        String sessionId = headerAccessor.getSessionId();
        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
        if (attrs == null) {
            String registeredSession = userService.getSessionId(sender);
            return registeredSession != null && registeredSession.equals(sessionId);
        }
        Object storedUser = attrs.get("username");
        if (storedUser != null) {
            return sender.equals(storedUser);
        }
        String registeredSession = userService.getSessionId(sender);
        if (registeredSession != null && registeredSession.equals(sessionId)) {
            attrs.put("username", sender);
            return true;
        }
        return false;
    }

    private void sendErrorToSession(SimpMessageHeaderAccessor headerAccessor, ChatMessage errorMessage) {
        if (headerAccessor.getSessionId() == null) {
            return;
        }
        sendToSessionQueue(headerAccessor.getSessionId(), "/queue/error", errorMessage);
    }

    private void sendPrivateToUserSession(String username, ChatMessage chatMessage) {
        String sessionId = userService.getSessionId(username);
        if (sessionId == null) {
            return;
        }
        sendToSessionQueue(sessionId, "/queue/private", chatMessage);
    }

    private void sendToSessionQueue(String sessionId, String destination, ChatMessage message) {
        SimpMessageHeaderAccessor messageHeaderAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        messageHeaderAccessor.setSessionId(sessionId);
        messageHeaderAccessor.setLeaveMutable(true);
        messagingTemplate.convertAndSendToUser(
                sessionId,
                destination,
                message,
                messageHeaderAccessor.getMessageHeaders()
        );
    }

    @MessageMapping("/queryAll")
    public void queryAll(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        if (verify(chatMessage, headerAccessor)) return;
        if (!chatMessage.getType().equals(Type.QUERY)) {
            chatMessage.setType(Type.ERROR);
            chatMessage.setContent("Type error");
            chatMessage.setTime(Kitimer.getCurrentTime());
            sendErrorToSession(headerAccessor, chatMessage);
            return;
        }
        if (!userService.containsUser(chatMessage.getSender())) return;
        chatMessage.setContent(userService.getAllUsers());
        chatMessage.setTime(Kitimer.getCurrentTime());
        if (headerAccessor.getSessionId() == null) {
            return;
        }
        sendToSessionQueue(headerAccessor.getSessionId(), "/queue/queryAll", chatMessage);
    }
}

package com.muimi.kichat.controller;

import com.muimi.kichat.Service.UserService;
import com.muimi.kichat.entity.ChatMessage;
import com.muimi.kichat.entity.Type;
import com.muimi.kichat.util.Kitimer;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Component
public class WebSocketEventListener {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String username = userService.getUser(sessionId);
        if (username != null) {
            userService.removeUser(username, sessionId);
            ChatMessage leaveMsg = new ChatMessage();
            leaveMsg.setType(Type.LEAVE);
            leaveMsg.setSender(username);
            leaveMsg.setContent(username + " left room.");
            leaveMsg.setTime(Kitimer.getCurrentTime());
            messagingTemplate.convertAndSend("/topic/public", leaveMsg);
        }
    }
}
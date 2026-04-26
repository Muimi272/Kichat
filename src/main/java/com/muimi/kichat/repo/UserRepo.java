package com.muimi.kichat.repo;

import org.springframework.stereotype.Repository;

import java.util.concurrent.*;
import java.util.*;

@Repository
public class UserRepo {
    private static final Map<String, String> userMap = new ConcurrentHashMap<>();
    private static final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    public boolean addUser(String username, String sessionId) {
        String existingUser = userSessionMap.putIfAbsent(sessionId, username);
        if (existingUser != null) return false;
        String existingSession = userMap.putIfAbsent(username, sessionId);
        if (existingSession != null) {
            userSessionMap.remove(sessionId, username);
            return false;
        }
        return true;
    }

    public boolean removeUser(String username, String sessionId) {
        if (userSessionMap.remove(sessionId, username)) {
            userMap.remove(username, sessionId);
            return true;
        }
        return false;
    }

    public boolean containsUser(String username) {
        return userMap.containsKey(username);
    }

    public List<String> getUsers() {
        return new ArrayList<>(userMap.keySet());
    }

    public String getUser(String sessionId) {
        return userSessionMap.get(sessionId);
    }

    public String getSessionId(String username) {
        return userMap.get(username);
    }
}

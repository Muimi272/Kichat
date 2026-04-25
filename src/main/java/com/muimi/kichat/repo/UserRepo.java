package com.muimi.kichat.repo;

import org.springframework.stereotype.Repository;

import java.util.concurrent.*;
import java.util.*;

@Repository
public class UserRepo {
    private static final Set<String> userSet = ConcurrentHashMap.newKeySet();
    private static final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    public boolean addUser(String username) {
        return userSet.add(username);
    }

    public boolean removeUser(String username) {
        userSessionMap.remove(username);
        return userSet.remove(username);
    }

    public boolean containsUser(String username) {
        return userSet.contains(username);
    }

    public List<String> getUsers() {
        return new ArrayList<>(userSet);
    }

    public void bindSession(String username, String sessionId) {
        if (username == null || sessionId == null) {
            return;
        }
        userSessionMap.put(username, sessionId);
    }

    public String getSessionId(String username) {
        return userSessionMap.get(username);
    }

    public void unbindSession(String username) {
        userSessionMap.remove(username);
    }
}

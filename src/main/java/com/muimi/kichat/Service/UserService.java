package com.muimi.kichat.Service;

import com.muimi.kichat.repo.UserRepo;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean addUser(String username, String sessionId) {
        return userRepo.addUser(username, sessionId);
    }

    public boolean removeUser(String username, String sessionId) {
        return userRepo.removeUser(username, sessionId);
    }

    public boolean containsUser(String username) {
        return userRepo.containsUser(username);
    }

    public String getAllUsers() {
        StringBuilder sb = new StringBuilder();
        for (String userName : userRepo.getUsers()) {
            sb.append("[").append(userName).append("]");
        }
        return sb.toString();
    }


    public String getUser(String sessionId) {
        return userRepo.getUser(sessionId);
    }

    public String getSessionId(String username) {
        return userRepo.getSessionId(username);
    }
}

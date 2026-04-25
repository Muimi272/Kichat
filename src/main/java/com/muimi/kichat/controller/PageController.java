package com.muimi.kichat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/join")
    public String joinPage() {
        return "join";
    }

    @GetMapping("/room")
    public String roomPage() {
        return "room";
    }
}

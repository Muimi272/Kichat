package com.muimi.kichat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/kichat")
    public String joinPage() {
        return "room";
    }

    @GetMapping("/")
    public String roomPage() {
        return "room";
    }
}

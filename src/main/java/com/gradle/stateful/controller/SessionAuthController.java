package com.gradle.stateful.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/stateful")
public class SessionAuthController {

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                        HttpServletRequest request) {

        HttpSession session = request.getSession();
        session.setAttribute("user", username);

        return ResponseEntity.ok("Session created for " + username);
    }

    @GetMapping("/profile")
    public ResponseEntity<String> profile(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return ResponseEntity.ok("Welcome " + session.getAttribute("user"));
    }
}
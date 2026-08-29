package com.gradle.stateless.controller;

import com.gradle.stateless.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stateless")
public class JwtAuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("username") String username) {  // ← ONLY THIS LINE CHANGED

        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/profile")
    public ResponseEntity<String> profile(@RequestHeader("Authorization") String header) {

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing token");
        }

        String token = header.substring(7);

        try {
            String user = jwtUtil.extractUser(token);
            return ResponseEntity.ok("Welcome " + user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
}
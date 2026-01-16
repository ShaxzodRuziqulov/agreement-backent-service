package com.example.agreement.controller;

import com.example.agreement.entity.NotificationLog;
import com.example.agreement.service.NotificationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationLogController {

    private final NotificationLogService service;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationLog>> byUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.findByUser(userId));
    }
}


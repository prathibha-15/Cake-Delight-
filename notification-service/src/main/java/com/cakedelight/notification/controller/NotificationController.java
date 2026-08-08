package com.cakedelight.notification.controller;

import com.cakedelight.notification.dto.NotificationResponse;
import com.cakedelight.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications/{orderId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(notificationService.getNotificationsByOrderId(orderId));
    }
}
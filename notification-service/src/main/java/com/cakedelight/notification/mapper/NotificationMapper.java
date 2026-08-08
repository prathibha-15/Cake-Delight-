package com.cakedelight.notification.mapper;

import com.cakedelight.notification.dto.NotificationResponse;
import com.cakedelight.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getEventId(),
                notification.getOrderId(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getSentAt(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
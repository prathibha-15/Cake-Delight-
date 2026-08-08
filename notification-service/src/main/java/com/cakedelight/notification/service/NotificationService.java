package com.cakedelight.notification.service;

import com.cakedelight.notification.dto.NotificationResponse;
import com.cakedelight.notification.event.OrderCompletedEvent;

import java.util.List;

public interface NotificationService {

    NotificationResponse handleOrderCompleted(OrderCompletedEvent event);

    List<NotificationResponse> getNotificationsByOrderId(Long orderId);
}
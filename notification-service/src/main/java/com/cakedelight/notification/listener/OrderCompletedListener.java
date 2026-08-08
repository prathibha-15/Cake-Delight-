package com.cakedelight.notification.listener;

import com.cakedelight.notification.config.RabbitMQConfig;
import com.cakedelight.notification.event.OrderCompletedEvent;
import com.cakedelight.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCompletedListener {

    private final NotificationService notificationService;

    public OrderCompletedListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        notificationService.handleOrderCompleted(event);
    }
}
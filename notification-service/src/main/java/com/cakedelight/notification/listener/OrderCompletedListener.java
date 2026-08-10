package com.cakedelight.notification.listener;

import com.cakedelight.notification.config.RabbitMQConfig;
import com.cakedelight.notification.event.OrderCompletedEvent;
import com.cakedelight.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCompletedListener.class);

    private final NotificationService notificationService;

    public OrderCompletedListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Received OrderCompletedEvent for order ID: {}", event != null ? event.getOrderId() : null);
        notificationService.handleOrderCompleted(event);
    }
}
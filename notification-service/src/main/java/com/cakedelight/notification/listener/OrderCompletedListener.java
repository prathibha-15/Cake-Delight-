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
        if (event == null || event.getEventId() == null || event.getOrderId() == null) {
            log.warn("Received invalid or null OrderCompletedEvent, skipping processing.");
            return;
        }
        log.info("Received OrderCompletedEvent for order ID: {}, event ID: {}", event.getOrderId(), event.getEventId());
        try {
            notificationService.handleOrderCompleted(event);
        } catch (Exception ex) {
            log.error("Unhandled error processing OrderCompletedEvent for order ID: {}. Error: {}", event.getOrderId(), ex.getMessage(), ex);
        }
    }
}
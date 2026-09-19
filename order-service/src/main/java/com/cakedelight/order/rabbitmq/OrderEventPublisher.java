package com.cakedelight.order.rabbitmq;

import com.cakedelight.order.config.RabbitMQConfig;
import com.cakedelight.order.event.OrderCompletedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderEventPublisher.class);

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 500L;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderCompletedEvent event) {
        int attempt = 0;
        boolean published = false;

        while (attempt < MAX_ATTEMPTS && !published) {
            attempt++;
            try {
                log.info("Publishing OrderCompletedEvent for order ID: {} to exchange: {} (Attempt {}/{})",
                        event.getOrderId(), RabbitMQConfig.ORDER_COMPLETED_EXCHANGE, attempt, MAX_ATTEMPTS);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ORDER_COMPLETED_EXCHANGE,
                        RabbitMQConfig.ORDER_COMPLETED_ROUTING_KEY,
                        event
                );
                published = true;
                log.info("Successfully published OrderCompletedEvent for order ID: {}", event.getOrderId());
            } catch (Exception ex) {
                log.warn("Attempt {}/{} to publish OrderCompletedEvent for order ID: {} failed: {}",
                        attempt, MAX_ATTEMPTS, event.getOrderId(), ex.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_BACKOFF_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("Final attempt to publish OrderCompletedEvent for order ID: {} failed completely. Error: {}",
                            event.getOrderId(), ex.getMessage(), ex);
                }
            }
        }
    }
}

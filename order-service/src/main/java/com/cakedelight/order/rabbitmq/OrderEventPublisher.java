package com.cakedelight.order.rabbitmq;

import com.cakedelight.order.config.RabbitMQConfig;
import com.cakedelight.order.event.OrderCompletedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderCompletedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_COMPLETED_EXCHANGE,
                RabbitMQConfig.ORDER_COMPLETED_ROUTING_KEY,
                event
        );
    }
}

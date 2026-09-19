package com.cakedelight.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_COMPLETED_QUEUE = "notification.order.completed";
    public static final String ORDER_COMPLETED_EXCHANGE = "cake-delight.exchange";
    public static final String ORDER_COMPLETED_ROUTING_KEY = "order.completed";

    public static final String DLX_EXCHANGE = "cake-delight.dlx";
    public static final String DLQ_ROUTING_KEY = "notification.order.completed.dlq";

    @Bean
    public DirectExchange orderCompletedExchange() {
        return new DirectExchange(ORDER_COMPLETED_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCompletedQueue() {
        return QueueBuilder.durable(ORDER_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderCompletedBinding() {
        return BindingBuilder
                .bind(orderCompletedQueue())
                .to(orderCompletedExchange())
                .with(ORDER_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}


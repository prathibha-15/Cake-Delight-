package com.cakedelight.notification.service.impl;

import com.cakedelight.notification.config.NotificationMailProperties;
import com.cakedelight.notification.dto.NotificationEmailPayload;
import com.cakedelight.notification.entity.Notification;
import com.cakedelight.notification.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final NotificationMailProperties mailProperties;

    public EmailNotificationSender(JavaMailSender mailSender, NotificationMailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void send(Notification notification, NotificationEmailPayload payload) {
        if (!mailProperties.isEnabled()) {
            log.info("Email notification disabled by configuration for order ID: {}", payload.getOrderId());
            return;
        }

        log.info("Dispatching email for order ID: {} to {}", payload.getOrderId(), mailProperties.getTo());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getFrom());
        message.setTo(mailProperties.getTo());
        message.setSubject(mailProperties.getSubjectPrefix() + " Order #" + payload.getOrderId() + " completed");
        message.setText(buildBody(notification, payload));
        mailSender.send(message);
        log.info("Email sent successfully for order ID: {}", payload.getOrderId());
    }

    private String buildBody(Notification notification, NotificationEmailPayload payload) {
        return "Order completed notification\n\n"
                + "Notification ID: " + notification.getId() + "\n"
                + "Event ID: " + payload.getEventId() + "\n"
                + "Order ID: " + payload.getOrderId() + "\n"
                + "Order Date: " + payload.getOrderDate() + "\n"
                + "Total Amount: " + payload.getTotalAmount() + "\n"
                + "Order Status: " + payload.getStatus() + "\n"
                + "Delivery Status: " + resolveDeliveryStatus(payload.getStatus()) + "\n"
                + "Channel: " + notification.getChannel() + "\n"
                + "Status: " + notification.getStatus();
    }

    private String resolveDeliveryStatus(String orderStatus) {
        if (orderStatus == null) {
            return "Pending";
        }

        String normalized = orderStatus.trim().toUpperCase();
        return switch (normalized) {
            case "DELIVERED" -> "Delivered";
            case "SHIPPED", "IN_TRANSIT", "PROCESSING", "CONFIRMED" -> "In transit";
            default -> "Pending";
        };
    }
}
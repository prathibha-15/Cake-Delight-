package com.cakedelight.notification.service.impl;

import com.cakedelight.notification.dto.NotificationEmailPayload;
import com.cakedelight.notification.dto.NotificationResponse;
import com.cakedelight.notification.entity.Notification;
import com.cakedelight.notification.entity.NotificationStatus;
import com.cakedelight.notification.event.OrderCompletedEvent;
import com.cakedelight.notification.mapper.NotificationMapper;
import com.cakedelight.notification.repository.NotificationRepository;
import com.cakedelight.notification.service.NotificationSender;
import com.cakedelight.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final String EMAIL_CHANNEL = "EMAIL";

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationSender notificationSender;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationMapper notificationMapper,
            NotificationSender notificationSender
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.notificationSender = notificationSender;
    }

    @Override
    public NotificationResponse handleOrderCompleted(OrderCompletedEvent event) {
        Notification existing = notificationRepository.findByEventId(event.getEventId())
                .orElse(null);

        if (existing != null && existing.getStatus() == NotificationStatus.SENT) {
            log.info("Duplicate completed event ignored for event ID: {}", event.getEventId());
            return notificationMapper.toResponse(existing);
        }

        LocalDateTime now = LocalDateTime.now();
        Notification notification;
        if (existing != null) {
            log.info("Retrying notification for failed/pending event ID: {}", event.getEventId());
            notification = existing;
        } else {
            notification = new Notification();
            notification.setEventId(event.getEventId());
            notification.setOrderId(event.getOrderId());
            notification.setChannel(EMAIL_CHANNEL);
            notification.setCreatedAt(now);
            notification.setSentAt(now);
        }

        notification.setStatus(NotificationStatus.PENDING);
        notification.setUpdatedAt(now);

        Notification saved = notificationRepository.save(notification);

        NotificationEmailPayload payload = new NotificationEmailPayload(
                event.getEventId(),
                event.getOrderId(),
                event.getOrderDate(),
                event.getTotalAmount(),
                event.getStatus()
        );

        try {
            notificationSender.send(saved, payload);
            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            log.info("Successfully sent notification for order ID: {}", event.getOrderId());
            return notificationMapper.toResponse(notificationRepository.save(saved));
        } catch (Exception e) {
            log.error("Failed notification attempt for order ID: {}. Error: {}", event.getOrderId(), e.getMessage());
            saved.setStatus(NotificationStatus.FAILED);
            saved.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(saved);
            throw e; // Rethrow to allow Spring AMQP retries to trigger
        }
    }

    @Override
    public List<NotificationResponse> getNotificationsByOrderId(Long orderId) {
        return notificationRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }
}
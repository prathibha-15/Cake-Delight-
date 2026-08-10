package com.cakedelight.notification.service;

import com.cakedelight.notification.dto.NotificationEmailPayload;
import com.cakedelight.notification.dto.NotificationResponse;
import com.cakedelight.notification.entity.Notification;
import com.cakedelight.notification.entity.NotificationStatus;
import com.cakedelight.notification.event.OrderCompletedEvent;
import com.cakedelight.notification.mapper.NotificationMapper;
import com.cakedelight.notification.repository.NotificationRepository;
import com.cakedelight.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private OrderCompletedEvent sampleEvent;
    private Notification sampleNotification;
    private NotificationResponse sampleResponse;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        sampleEvent = new OrderCompletedEvent(eventId, 100L, LocalDateTime.now(), 1598.0, "CREATED");

        sampleNotification = new Notification();
        sampleNotification.setId(1L);
        sampleNotification.setEventId(eventId);
        sampleNotification.setOrderId(100L);
        sampleNotification.setChannel("EMAIL");
        sampleNotification.setStatus(NotificationStatus.SENT);
        sampleNotification.setCreatedAt(LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        sampleResponse = new NotificationResponse(1L, eventId, 100L, "EMAIL", NotificationStatus.SENT, now, now, now);
    }

    @Test
    void handleOrderCompleted_ExistingNotification_ShouldReturnExistingResponse() {
        when(notificationRepository.findByEventId(eventId)).thenReturn(Optional.of(sampleNotification));
        when(notificationMapper.toResponse(sampleNotification)).thenReturn(sampleResponse);

        NotificationResponse response = notificationService.handleOrderCompleted(sampleEvent);

        assertNotNull(response);
        assertEquals(eventId, response.getEventId());
        verify(notificationSender, never()).send(any(), any());
    }

    @Test
    void handleOrderCompleted_NewNotification_ShouldSaveAndSendEmail() {
        when(notificationRepository.findByEventId(eventId)).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);
        doNothing().when(notificationSender).send(any(Notification.class), any(NotificationEmailPayload.class));
        when(notificationMapper.toResponse(sampleNotification)).thenReturn(sampleResponse);

        NotificationResponse response = notificationService.handleOrderCompleted(sampleEvent);

        assertNotNull(response);
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(notificationSender).send(any(Notification.class), any(NotificationEmailPayload.class));
    }

    @Test
    void getNotificationsByOrderId_ShouldReturnList() {
        when(notificationRepository.findByOrderIdOrderByCreatedAtDesc(100L)).thenReturn(List.of(sampleNotification));
        when(notificationMapper.toResponse(sampleNotification)).thenReturn(sampleResponse);

        List<NotificationResponse> result = notificationService.getNotificationsByOrderId(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getOrderId());
    }
}

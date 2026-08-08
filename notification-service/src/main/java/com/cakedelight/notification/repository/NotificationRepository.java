package com.cakedelight.notification.repository;

import com.cakedelight.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByEventId(UUID eventId);

    List<Notification> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
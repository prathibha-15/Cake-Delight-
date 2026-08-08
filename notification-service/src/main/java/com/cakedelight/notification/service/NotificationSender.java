package com.cakedelight.notification.service;

import com.cakedelight.notification.dto.NotificationEmailPayload;
import com.cakedelight.notification.entity.Notification;

public interface NotificationSender {

    void send(Notification notification, NotificationEmailPayload payload);
}
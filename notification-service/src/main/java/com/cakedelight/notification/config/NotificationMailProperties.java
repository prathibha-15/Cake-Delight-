package com.cakedelight.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "notification.mail")
public class NotificationMailProperties {

    private boolean enabled = true;
    private String from = "no-reply@cakedelight.local";
    private String to = "demo@cakedelight.local";
    private String subjectPrefix = "[Cake Delight]";
}
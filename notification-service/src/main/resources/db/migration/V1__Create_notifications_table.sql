CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BINARY(16) NOT NULL,
    order_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    sent_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_notification_event_id UNIQUE (event_id),
    INDEX idx_notification_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

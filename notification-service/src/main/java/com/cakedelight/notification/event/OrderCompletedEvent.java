package com.cakedelight.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID eventId;
    private Long orderId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
}
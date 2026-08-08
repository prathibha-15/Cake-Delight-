package com.cakedelight.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {

    private UUID eventId;
    private Long orderId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
}

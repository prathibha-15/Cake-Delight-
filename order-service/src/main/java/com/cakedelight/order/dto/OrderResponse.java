package com.cakedelight.order.dto;

import com.cakedelight.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;

    private Double totalAmount;

    private OrderStatus status;

    private LocalDateTime orderDate;
}
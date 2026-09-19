package com.cakedelight.order.service;

import com.cakedelight.order.dto.CheckoutResponse;
import com.cakedelight.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    CheckoutResponse checkout(Long userId);

    OrderResponse getOrder(Long orderId, Long userId, String userRole);

    List<OrderResponse> getUserOrders(Long userId);
}
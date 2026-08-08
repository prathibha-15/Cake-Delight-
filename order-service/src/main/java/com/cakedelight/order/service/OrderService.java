package com.cakedelight.order.service;

import com.cakedelight.order.dto.CheckoutResponse;
import com.cakedelight.order.dto.OrderResponse;

public interface OrderService {

    CheckoutResponse checkout();

    OrderResponse getOrder(Long orderId);
}
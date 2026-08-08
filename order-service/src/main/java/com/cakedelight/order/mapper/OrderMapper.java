package com.cakedelight.order.mapper;

import com.cakedelight.order.dto.BasketItemResponse;
import com.cakedelight.order.dto.OrderResponse;
import com.cakedelight.order.entity.BasketItem;
import com.cakedelight.order.entity.Order;

public class OrderMapper {

    public static BasketItemResponse toBasketResponse(BasketItem item) {

        return new BasketItemResponse(
                item.getId(),
                item.getCakeId(),
                item.getCakeName(),
                item.getPriceSnapshot(),
                item.getQuantity(),
                item.getPriceSnapshot() * item.getQuantity()
        );
    }

    public static OrderResponse toOrderResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate()
        );
    }
}
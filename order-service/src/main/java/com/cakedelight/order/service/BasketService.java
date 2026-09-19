package com.cakedelight.order.service;

import com.cakedelight.order.dto.BasketItemRequest;
import com.cakedelight.order.dto.BasketItemResponse;
import com.cakedelight.order.dto.BasketResponse;

public interface BasketService {

    BasketItemResponse addToBasket(Long userId, BasketItemRequest request);

    BasketItemResponse updateBasketItem(Long userId, Long itemId, BasketItemRequest request);

    void removeBasketItem(Long userId, Long itemId);

    BasketResponse getBasket(Long userId);
}
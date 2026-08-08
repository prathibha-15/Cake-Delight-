package com.cakedelight.order.service;

import com.cakedelight.order.dto.BasketItemRequest;
import com.cakedelight.order.dto.BasketItemResponse;
import com.cakedelight.order.dto.BasketResponse;

public interface BasketService {

    BasketItemResponse addToBasket(BasketItemRequest request);

    BasketItemResponse updateBasketItem(Long itemId, BasketItemRequest request);

    void removeBasketItem(Long itemId);

    BasketResponse getBasket();
}
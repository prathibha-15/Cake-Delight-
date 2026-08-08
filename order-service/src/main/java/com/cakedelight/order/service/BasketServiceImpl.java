package com.cakedelight.order.service;

import com.cakedelight.order.dto.BasketItemRequest;
import com.cakedelight.order.dto.BasketItemResponse;
import com.cakedelight.order.dto.BasketResponse;
import com.cakedelight.order.entity.BasketItem;
import com.cakedelight.order.exception.BasketItemNotFoundException;
import com.cakedelight.order.mapper.OrderMapper;
import com.cakedelight.order.repository.BasketItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BasketServiceImpl implements BasketService {

    private final BasketItemRepository basketRepository;

    public BasketServiceImpl(BasketItemRepository basketRepository) {
        this.basketRepository = basketRepository;
    }

    @Override
    public BasketItemResponse addToBasket(BasketItemRequest request) {

        BasketItem item = new BasketItem();

        item.setCakeId(request.getCakeId());

        // Temporary values.
        // Later these will come from Catalog Service using RestClient.
        item.setCakeName("Sample Cake");
        item.setPriceSnapshot(500.0);

        item.setQuantity(request.getQuantity());

        BasketItem saved = basketRepository.save(item);

        return OrderMapper.toBasketResponse(saved);
    }

    @Override
    public BasketItemResponse updateBasketItem(Long itemId, BasketItemRequest request) {

        BasketItem item = basketRepository.findById(itemId)
                .orElseThrow(() ->
                        new BasketItemNotFoundException("Basket item not found"));

        item.setQuantity(request.getQuantity());

        BasketItem updated = basketRepository.save(item);

        return OrderMapper.toBasketResponse(updated);
    }

    @Override
    public void removeBasketItem(Long itemId) {

        basketRepository.deleteById(itemId);
    }

    @Override
    public BasketResponse getBasket() {

        List<BasketItem> items = basketRepository.findAll();

        List<BasketItemResponse> responses = items.stream()
                .map(OrderMapper::toBasketResponse)
                .toList();

        double total = responses.stream()
                .mapToDouble(BasketItemResponse::getSubtotal)
                .sum();

        return new BasketResponse(responses, total);
    }
}
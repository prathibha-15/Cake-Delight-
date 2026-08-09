package com.cakedelight.order.service;

import com.cakedelight.order.dto.BasketItemRequest;
import com.cakedelight.order.dto.BasketItemResponse;
import com.cakedelight.order.dto.BasketResponse;
import com.cakedelight.order.dto.CakeDetailsResponse;
import com.cakedelight.order.entity.BasketItem;
import com.cakedelight.order.exception.BasketItemNotFoundException;
import com.cakedelight.order.exception.CakeNotFoundException;
import com.cakedelight.order.mapper.OrderMapper;
import com.cakedelight.order.repository.BasketItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BasketServiceImpl implements BasketService {

    private final BasketItemRepository basketRepository;
    private final RestClient catalogClient;

    public BasketServiceImpl(
            BasketItemRepository basketRepository,
            @Value("${catalog.service.base-url:http://catalog-service:8081}") String catalogServiceBaseUrl) {
        this.basketRepository = basketRepository;
        this.catalogClient = RestClient.builder()
                .baseUrl(catalogServiceBaseUrl)
                .build();
    }

    @Override
    public BasketItemResponse addToBasket(BasketItemRequest request) {

        CakeDetailsResponse cake = fetchCake(request.getCakeId());

        BasketItem item = new BasketItem();

        item.setCakeId(request.getCakeId());
        item.setCakeName(cake.name());
        item.setPriceSnapshot(cake.price());

        item.setQuantity(request.getQuantity());

        BasketItem saved = basketRepository.save(item);

        return OrderMapper.toBasketResponse(saved);
    }

    @Override
    public BasketItemResponse updateBasketItem(Long itemId, BasketItemRequest request) {

        BasketItem item = basketRepository.findById(itemId)
                .orElseThrow(() ->
                        new BasketItemNotFoundException("Basket item not found"));

        if (!item.getCakeId().equals(request.getCakeId())) {
            CakeDetailsResponse cake = fetchCake(request.getCakeId());
            item.setCakeId(request.getCakeId());
            item.setCakeName(cake.name());
            item.setPriceSnapshot(cake.price());
        }

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

    private CakeDetailsResponse fetchCake(Long cakeId) {
        try {
            return catalogClient.get()
                    .uri("/api/cakes/{id}", cakeId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new CakeNotFoundException("Cake not found");
                    })
                    .body(CakeDetailsResponse.class);
        } catch (RuntimeException ex) {
            throw new CakeNotFoundException("Cake not found");
        }
    }
}
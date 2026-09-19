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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BasketServiceImpl implements BasketService {

    private final BasketItemRepository basketRepository;
    private final RestClient catalogClient;

    public BasketServiceImpl(
            BasketItemRepository basketRepository,
            @Value("${catalog.service.base-url:http://catalog-service:8081}") String catalogServiceBaseUrl,
            @Value("${gateway.internal-secret:c2VjcmV0LWtleS1jYWtlLWRlbGlnaHQtdjItc3VwZXItc2VjcmV0LXNlY3JldC1rZXk=}") String internalSecret) {
        this.basketRepository = basketRepository;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);

        this.catalogClient = RestClient.builder()
                .baseUrl(catalogServiceBaseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Secret", internalSecret)
                .build();
    }

    @Override
    public BasketItemResponse addToBasket(Long userId, BasketItemRequest request) {

        CakeDetailsResponse cake = fetchCake(request.getCakeId());

        BasketItem item = basketRepository.findByUserIdAndCakeId(userId, request.getCakeId())
                .orElseGet(() -> {
                    BasketItem newItem = new BasketItem();
                    newItem.setUserId(userId);
                    newItem.setCakeId(request.getCakeId());
                    newItem.setCakeName(cake.name());
                    newItem.setPriceSnapshot(cake.price());
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + request.getQuantity());

        BasketItem saved = basketRepository.save(item);

        return OrderMapper.toBasketResponse(saved);
    }

    @Override
    public BasketItemResponse updateBasketItem(Long userId, Long itemId, BasketItemRequest request) {

        BasketItem item = basketRepository.findByIdAndUserId(itemId, userId)
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
    public void removeBasketItem(Long userId, Long itemId) {

        BasketItem item = basketRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() ->
                        new BasketItemNotFoundException("Basket item not found"));

        basketRepository.delete(item);
    }

    @Override
    public BasketResponse getBasket(Long userId) {

        List<BasketItem> items = basketRepository.findByUserId(userId);

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
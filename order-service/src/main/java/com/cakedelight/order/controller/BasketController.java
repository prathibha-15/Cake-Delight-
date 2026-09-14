package com.cakedelight.order.controller;

import com.cakedelight.order.dto.BasketItemRequest;
import com.cakedelight.order.dto.BasketItemResponse;
import com.cakedelight.order.dto.BasketResponse;
import com.cakedelight.order.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.order.exception.UnauthorizedAccessException;

@RestController
@RequestMapping("/api/basket")
@Tag(name = "Basket API", description = "Manage shopping basket")
public class BasketController {

    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @Operation(summary = "Add item to basket")
    @PostMapping
    public ResponseEntity<BasketItemResponse> addToBasket(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody BasketItemRequest request) {

        validateUserId(userId);
        return new ResponseEntity<>(
                basketService.addToBasket(userId, request),
                HttpStatus.CREATED
        );
    }

    @Operation(summary = "Update basket item")
    @PutMapping("/{itemId}")
    public ResponseEntity<BasketItemResponse> updateBasketItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody BasketItemRequest request) {

        validateUserId(userId);
        return ResponseEntity.ok(
                basketService.updateBasketItem(userId, itemId, request)
        );
    }

    @Operation(summary = "Remove basket item")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeBasketItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long itemId) {

        validateUserId(userId);
        basketService.removeBasketItem(userId, itemId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "View basket")
    @GetMapping
    public ResponseEntity<BasketResponse> getBasket(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        validateUserId(userId);
        return ResponseEntity.ok(
                basketService.getBasket(userId)
        );
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new UnauthorizedAccessException("Missing or invalid user identity header");
        }
    }
}
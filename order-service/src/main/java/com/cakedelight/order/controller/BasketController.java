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
            @Valid @RequestBody BasketItemRequest request) {

        return new ResponseEntity<>(
                basketService.addToBasket(request),
                HttpStatus.CREATED
        );
    }

    @Operation(summary = "Update basket item")
    @PutMapping("/{itemId}")
    public ResponseEntity<BasketItemResponse> updateBasketItem(
            @PathVariable Long itemId,
            @Valid @RequestBody BasketItemRequest request) {

        return ResponseEntity.ok(
                basketService.updateBasketItem(itemId, request)
        );
    }

    @Operation(summary = "Remove basket item")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeBasketItem(
            @PathVariable Long itemId) {

        basketService.removeBasketItem(itemId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "View basket")
    @GetMapping
    public ResponseEntity<BasketResponse> getBasket() {

        return ResponseEntity.ok(
                basketService.getBasket()
        );
    }
}
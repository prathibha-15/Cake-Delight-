package com.cakedelight.order.controller;

import com.cakedelight.order.dto.CheckoutResponse;
import com.cakedelight.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.order.exception.UnauthorizedAccessException;

@RestController
@RequestMapping("/api")
@Tag(name = "Checkout API")
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout basket")
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        if (userId == null) {
            throw new UnauthorizedAccessException("Missing or invalid user identity header");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(userId));
    }
}
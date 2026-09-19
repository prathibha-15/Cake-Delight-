package com.cakedelight.order.controller;

import com.cakedelight.order.dto.OrderResponse;
import com.cakedelight.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.order.exception.UnauthorizedAccessException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order API")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders for authenticated user")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        if (userId == null) {
            throw new UnauthorizedAccessException("Missing or invalid user identity header");
        }

        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable Long id) {

        if (userId == null) {
            throw new UnauthorizedAccessException("Missing or invalid user identity header");
        }

        return ResponseEntity.ok(orderService.getOrder(id, userId, userRole));
    }
}
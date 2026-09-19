package com.cakedelight.order.service;

import com.cakedelight.order.dto.CheckoutResponse;
import com.cakedelight.order.dto.OrderResponse;
import com.cakedelight.order.entity.BasketItem;
import com.cakedelight.order.entity.Order;
import com.cakedelight.order.entity.OrderItem;
import com.cakedelight.order.enums.OrderStatus;
import com.cakedelight.order.exception.OrderNotFoundException;
import com.cakedelight.order.event.OrderCompletedEvent;
import com.cakedelight.order.mapper.OrderMapper;
import com.cakedelight.order.rabbitmq.OrderEventPublisher;
import com.cakedelight.order.repository.BasketItemRepository;
import com.cakedelight.order.repository.OrderItemRepository;
import com.cakedelight.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final BasketItemRepository basketRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderServiceImpl(BasketItemRepository basketRepository,
                            OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            OrderEventPublisher orderEventPublisher) {

        this.basketRepository = basketRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    @Transactional
    public CheckoutResponse checkout(Long userId) {

        List<BasketItem> basketItems = basketRepository.findByUserId(userId);

        if (basketItems.isEmpty()) {
            throw new RuntimeException("Basket is empty");
        }

        double totalAmount = 0;

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> orderItems = new ArrayList<>();

        for (BasketItem basketItem : basketItems) {

            OrderItem orderItem = new OrderItem();

            orderItem.setCakeId(basketItem.getCakeId());
            orderItem.setCakeName(basketItem.getCakeName());
            orderItem.setPriceSnapshot(basketItem.getPriceSnapshot());
            orderItem.setQuantity(basketItem.getQuantity());

            orderItem.setOrder(order);

            orderItems.add(orderItem);

            totalAmount += basketItem.getPriceSnapshot() * basketItem.getQuantity();
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        orderItemRepository.saveAll(orderItems);

        log.info("Created order ID: {} for user ID: {} with total amount: {}", savedOrder.getId(), userId, savedOrder.getTotalAmount());

        OrderCompletedEvent orderCompletedEvent = new OrderCompletedEvent(
                UUID.randomUUID(),
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getOrderDate(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus().name()
        );

        orderEventPublisher.publish(orderCompletedEvent);
        log.info("Published OrderCompletedEvent for order ID: {}", savedOrder.getId());

        basketRepository.deleteByUserId(userId);

        OrderResponse response = OrderMapper.toOrderResponse(savedOrder);

        return new CheckoutResponse(
                "Order placed successfully",
                response
        );
    }

    @Override
    public OrderResponse getOrder(Long orderId, Long userId, String userRole) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found"));

        if ("ROLE_ADMIN".equals(userRole)) {
            return OrderMapper.toOrderResponse(order);
        }

        if (order.getUserId() != null && order.getUserId().equals(userId)) {
            return OrderMapper.toOrderResponse(order);
        }

        throw new com.cakedelight.order.exception.ForbiddenAccessException("Access denied: You do not own this order");
    }

    @Override
    public List<OrderResponse> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
        return orders.stream()
                .map(OrderMapper::toOrderResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}
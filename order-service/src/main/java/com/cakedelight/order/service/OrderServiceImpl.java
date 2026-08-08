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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

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
    public CheckoutResponse checkout() {

        List<BasketItem> basketItems = basketRepository.findAll();

        if (basketItems.isEmpty()) {
            throw new RuntimeException("Basket is empty");
        }

        double totalAmount = 0;

        Order order = new Order();
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

        OrderCompletedEvent orderCompletedEvent = new OrderCompletedEvent(
                UUID.randomUUID(),
                savedOrder.getId(),
                savedOrder.getOrderDate(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus().name()
        );

        orderEventPublisher.publish(orderCompletedEvent);

        basketRepository.deleteAll();

        OrderResponse response = OrderMapper.toOrderResponse(savedOrder);

        return new CheckoutResponse(
                "Order placed successfully",
                response
        );
    }

    @Override
    public OrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found"));

        return OrderMapper.toOrderResponse(order);
    }
}
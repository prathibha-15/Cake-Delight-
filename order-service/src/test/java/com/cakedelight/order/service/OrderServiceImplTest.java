package com.cakedelight.order.service;

import com.cakedelight.order.dto.CheckoutResponse;
import com.cakedelight.order.dto.OrderResponse;
import com.cakedelight.order.entity.BasketItem;
import com.cakedelight.order.entity.Order;
import com.cakedelight.order.entity.OrderItem;
import com.cakedelight.order.enums.OrderStatus;
import com.cakedelight.order.event.OrderCompletedEvent;
import com.cakedelight.order.exception.OrderNotFoundException;
import com.cakedelight.order.rabbitmq.OrderEventPublisher;
import com.cakedelight.order.repository.BasketItemRepository;
import com.cakedelight.order.repository.OrderItemRepository;
import com.cakedelight.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private BasketItemRepository basketRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private BasketItem sampleBasketItem;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleBasketItem = new BasketItem();
        sampleBasketItem.setId(1L);
        sampleBasketItem.setUserId(1L);
        sampleBasketItem.setCakeId(10L);
        sampleBasketItem.setCakeName("Chocolate Truffle");
        sampleBasketItem.setPriceSnapshot(799.0);
        sampleBasketItem.setQuantity(2);

        sampleOrder = new Order();
        sampleOrder.setId(100L);
        sampleOrder.setUserId(1L);
        sampleOrder.setOrderDate(LocalDateTime.now());
        sampleOrder.setStatus(OrderStatus.CREATED);
        sampleOrder.setTotalAmount(1598.0);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setCakeId(10L);
        orderItem.setCakeName("Chocolate Truffle");
        orderItem.setPriceSnapshot(799.0);
        orderItem.setQuantity(2);
        orderItem.setOrder(sampleOrder);

        sampleOrder.setItems(List.of(orderItem));
    }

    @Test
    void checkout_WithBasketItems_ShouldCreateOrderAndPublishEvent() {
        when(basketRepository.findByUserId(1L)).thenReturn(List.of(sampleBasketItem));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        doNothing().when(orderEventPublisher).publish(any(OrderCompletedEvent.class));
        doNothing().when(basketRepository).deleteByUserId(1L);

        CheckoutResponse response = orderService.checkout(1L);

        assertNotNull(response);
        assertEquals("Order placed successfully", response.getMessage());
        assertNotNull(response.getOrder());
        assertEquals(100L, response.getOrder().getOrderId());

        verify(orderRepository).save(any(Order.class));
        verify(orderEventPublisher).publish(any(OrderCompletedEvent.class));
        verify(basketRepository).deleteByUserId(1L);
    }

    @Test
    void checkout_EmptyBasket_ShouldThrowException() {
        when(basketRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.checkout(1L));
        assertEquals("Basket is empty", ex.getMessage());
    }

    @Test
    void getOrder_ExistingIdAndOwner_ShouldReturnOrderResponse() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse response = orderService.getOrder(100L, 1L, "ROLE_USER");

        assertNotNull(response);
        assertEquals(100L, response.getOrderId());
    }

    @Test
    void getOrder_OtherUser_ShouldThrowForbiddenException() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(sampleOrder));

        assertThrows(com.cakedelight.order.exception.ForbiddenAccessException.class,
                () -> orderService.getOrder(100L, 2L, "ROLE_USER"));
    }

    @Test
    void getOrder_AdminAccess_ShouldReturnOrderResponse() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse response = orderService.getOrder(100L, 2L, "ROLE_ADMIN");

        assertNotNull(response);
        assertEquals(100L, response.getOrderId());
    }

    @Test
    void getOrder_NonExistingId_ShouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(999L, 1L, "ROLE_USER"));
    }

    @Test
    void getUserOrders_WithExistingOrders_ShouldReturnOrderList() {
        when(orderRepository.findByUserIdOrderByOrderDateDesc(1L)).thenReturn(List.of(sampleOrder));

        List<OrderResponse> responses = orderService.getUserOrders(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getOrderId());
        verify(orderRepository).findByUserIdOrderByOrderDateDesc(1L);
    }

    @Test
    void getUserOrders_WhenNoOrders_ShouldReturnEmptyList() {
        when(orderRepository.findByUserIdOrderByOrderDateDesc(2L)).thenReturn(Collections.emptyList());

        List<OrderResponse> responses = orderService.getUserOrders(2L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(orderRepository).findByUserIdOrderByOrderDateDesc(2L);
    }
}

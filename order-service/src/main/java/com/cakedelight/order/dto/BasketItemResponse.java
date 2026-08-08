package com.cakedelight.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BasketItemResponse {

    private Long id;

    private Long cakeId;

    private String cakeName;

    private Double priceSnapshot;

    private Integer quantity;

    private Double subtotal;
}
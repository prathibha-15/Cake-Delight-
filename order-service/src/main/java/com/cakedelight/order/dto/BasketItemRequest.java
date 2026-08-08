package com.cakedelight.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BasketItemRequest {

    @NotNull(message = "Cake ID is required")
    private Long cakeId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
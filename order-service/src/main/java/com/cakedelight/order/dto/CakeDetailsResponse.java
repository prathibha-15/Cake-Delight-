package com.cakedelight.order.dto;

public record CakeDetailsResponse(
        Long id,
        String name,
        String description,
        String category,
        Double price,
        Integer stock,
        String imageUrl
) {
}
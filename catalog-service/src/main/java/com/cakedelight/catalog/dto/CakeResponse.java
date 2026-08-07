package com.cakedelight.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CakeResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private Integer stock;
    private String imageUrl;
}
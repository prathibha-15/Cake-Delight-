package com.cakedelight.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "basket_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cakeId;

    @Column(nullable = false)
    private String cakeName;

    @Column(nullable = false)
    private Double priceSnapshot;

    @Column(nullable = false)
    private Integer quantity;
}
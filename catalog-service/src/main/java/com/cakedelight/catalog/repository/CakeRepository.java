package com.cakedelight.catalog.repository;

import com.cakedelight.catalog.entity.Cake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CakeRepository extends JpaRepository<Cake, Long> {

    List<Cake> findByCategory(String category);

    List<Cake> findByNameContainingIgnoreCase(String name);

    List<Cake> findByCategoryAndNameContainingIgnoreCase(String category, String name);

    List<Cake> findByPriceLessThanEqual(Double price);

    List<Cake> findByPriceBetween(Double minPrice, Double maxPrice);
}
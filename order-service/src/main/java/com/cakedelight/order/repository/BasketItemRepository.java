package com.cakedelight.order.repository;

import com.cakedelight.order.entity.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasketItemRepository extends JpaRepository<BasketItem, Long> {

    List<BasketItem> findByUserId(Long userId);

    Optional<BasketItem> findByUserIdAndCakeId(Long userId, Long cakeId);

    Optional<BasketItem> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
package com.cakedelight.catalog.repository;

import com.cakedelight.catalog.entity.Cake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CakeRepository extends JpaRepository<Cake, Long> {

}
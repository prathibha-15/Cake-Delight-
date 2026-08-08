package com.cakedelight.catalog.service;

import com.cakedelight.catalog.dto.CakeRequest;
import com.cakedelight.catalog.dto.CakeResponse;

import java.util.List;

public interface CakeService {

    CakeResponse createCake(CakeRequest request);

    List<CakeResponse> getAllCakes();

    List<CakeResponse> getCakes(String category, String name, Double minPrice, Double maxPrice);

    CakeResponse getCakeById(Long id);

    CakeResponse updateCake(Long id, CakeRequest request);

    void deleteCake(Long id);
}
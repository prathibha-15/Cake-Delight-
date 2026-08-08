package com.cakedelight.catalog.service.impl;
import com.cakedelight.catalog.exception.ResourceNotFoundException;
import com.cakedelight.catalog.dto.CakeRequest;
import com.cakedelight.catalog.dto.CakeResponse;
import com.cakedelight.catalog.entity.Cake;
import com.cakedelight.catalog.mapper.CakeMapper;
import com.cakedelight.catalog.repository.CakeRepository;
import com.cakedelight.catalog.service.CakeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CakeServiceImpl implements CakeService {

    private final CakeRepository cakeRepository;
    private final CakeMapper cakeMapper;

    public CakeServiceImpl(CakeRepository cakeRepository, CakeMapper cakeMapper) {
        this.cakeRepository = cakeRepository;
        this.cakeMapper = cakeMapper;
    }

    @Override
    public CakeResponse createCake(CakeRequest request) {
        Cake cake = new Cake();

        cake.setName(request.getName());
        cake.setDescription(request.getDescription());
        cake.setCategory(request.getCategory());
        cake.setPrice(request.getPrice());
        cake.setStock(request.getStock());
        cake.setImageUrl(request.getImageUrl());

        Cake savedCake = cakeRepository.save(cake);

        return cakeMapper.toResponse(savedCake);
    }

    @Override
    public List<CakeResponse> getAllCakes() {
        return cakeRepository.findAll()
                .stream()
                .map(cakeMapper::toResponse)
                .toList();
    }

    @Override
    public List<CakeResponse> getCakes(String category, String name, Double minPrice, Double maxPrice) {
        if (category != null && !category.isBlank() && name != null && !name.isBlank()) {
            return cakeRepository.findByCategoryAndNameContainingIgnoreCase(category, name)
                    .stream()
                    .map(cakeMapper::toResponse)
                    .toList();
        }

        if (category != null && !category.isBlank()) {
            return cakeRepository.findByCategory(category)
                    .stream()
                    .map(cakeMapper::toResponse)
                    .toList();
        }

        if (name != null && !name.isBlank()) {
            return cakeRepository.findByNameContainingIgnoreCase(name)
                    .stream()
                    .map(cakeMapper::toResponse)
                    .toList();
        }

        if (minPrice != null || maxPrice != null) {
            Double low = minPrice == null ? 0.0 : minPrice;
            Double high = maxPrice == null ? Double.MAX_VALUE : maxPrice;

            if (high < low) {
                throw new IllegalArgumentException("maxPrice cannot be less than minPrice");
            }

            return cakeRepository.findByPriceBetween(low, high)
                    .stream()
                    .map(cakeMapper::toResponse)
                    .toList();
        }

        return getAllCakes();
    }

    @Override
    public CakeResponse getCakeById(Long id) {
        Cake cake = cakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cake not found with id: " + id));

        return cakeMapper.toResponse(cake);
    }

    @Override
    public CakeResponse updateCake(Long id, CakeRequest request) {

        Cake cake = cakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cake not found with id: " + id));

        cake.setName(request.getName());
        cake.setDescription(request.getDescription());
        cake.setCategory(request.getCategory());
        cake.setPrice(request.getPrice());
        cake.setStock(request.getStock());
        cake.setImageUrl(request.getImageUrl());

        Cake updatedCake = cakeRepository.save(cake);

        return cakeMapper.toResponse(updatedCake);
    }

    @Override
    public void deleteCake(Long id) {
        cakeRepository.deleteById(id);
    }
}
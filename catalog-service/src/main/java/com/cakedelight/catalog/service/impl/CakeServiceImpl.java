package com.cakedelight.catalog.service.impl;
import com.cakedelight.catalog.exception.ResourceNotFoundException;
import com.cakedelight.catalog.dto.CakeRequest;
import com.cakedelight.catalog.dto.CakeResponse;
import com.cakedelight.catalog.entity.Cake;
import com.cakedelight.catalog.repository.CakeRepository;
import com.cakedelight.catalog.service.CakeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CakeServiceImpl implements CakeService {

    private final CakeRepository cakeRepository;

    public CakeServiceImpl(CakeRepository cakeRepository) {
        this.cakeRepository = cakeRepository;
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

        return mapToResponse(savedCake);
    }

    @Override
    public List<CakeResponse> getAllCakes() {
        return cakeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CakeResponse getCakeById(Long id) {
        Cake cake = cakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cake not found with id: " + id));

        return mapToResponse(cake);
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

        return mapToResponse(updatedCake);
    }

    @Override
    public void deleteCake(Long id) {
        cakeRepository.deleteById(id);
    }

    private CakeResponse mapToResponse(Cake cake) {

        return new CakeResponse(
                cake.getId(),
                cake.getName(),
                cake.getDescription(),
                cake.getCategory(),
                cake.getPrice(),
                cake.getStock(),
                cake.getImageUrl()
        );
    }
}
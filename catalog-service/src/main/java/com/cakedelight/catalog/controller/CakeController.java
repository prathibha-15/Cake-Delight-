package com.cakedelight.catalog.controller;

import com.cakedelight.catalog.dto.CakeRequest;
import com.cakedelight.catalog.dto.CakeResponse;
import com.cakedelight.catalog.service.CakeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cakes")
public class CakeController {

    private final CakeService cakeService;

    public CakeController(CakeService cakeService) {
        this.cakeService = cakeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CakeResponse createCake(@Valid @RequestBody CakeRequest request) {
        return cakeService.createCake(request);
    }

    @GetMapping
    public List<CakeResponse> getAllCakes() {
        return cakeService.getAllCakes();
    }

    @GetMapping("/{id}")
    public CakeResponse getCakeById(@PathVariable Long id) {
        return cakeService.getCakeById(id);
    }

    @PutMapping("/{id}")
    public CakeResponse updateCake(@PathVariable Long id,
                                   @Valid @RequestBody CakeRequest request) {
        return cakeService.updateCake(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCake(@PathVariable Long id) {
        cakeService.deleteCake(id);
    }
}
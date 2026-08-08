package com.cakedelight.catalog.controller;

import com.cakedelight.catalog.dto.CakeRequest;
import com.cakedelight.catalog.dto.CakeResponse;
import com.cakedelight.catalog.service.CakeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
@RestController
@RequestMapping("/api/cakes")
@Tag(name = "Catalog API", description = "Operations related to cake catalog")
public class CakeController {

    private final CakeService cakeService;

    public CakeController(CakeService cakeService) {
        this.cakeService = cakeService;
    }

    @Operation(summary = "Create a new cake")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CakeResponse createCake(@Valid @RequestBody CakeRequest request) {
        return cakeService.createCake(request);
    }

    @Operation(summary = "Get all cakes with optional filters")
    @GetMapping
    public List<CakeResponse> getAllCakes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return cakeService.getCakes(category, name, minPrice, maxPrice);
    }

    @Operation(summary = "Get a cake by id")
    @GetMapping("/{id}")
    public CakeResponse getCakeById(@PathVariable Long id) {
        return cakeService.getCakeById(id);
    }

    @Operation(summary = "Update an existing cake")
    @PutMapping("/{id}")
    public CakeResponse updateCake(@PathVariable Long id,
                                   @Valid @RequestBody CakeRequest request) {
        return cakeService.updateCake(id, request);
    }

    @Operation(summary = "Delete a cake")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCake(@PathVariable Long id) {
        cakeService.deleteCake(id);
    }
}
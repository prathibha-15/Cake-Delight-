package com.cakedelight.rating.controller;

import com.cakedelight.rating.dto.AverageRatingResponse;
import com.cakedelight.rating.dto.RatingRequest;
import com.cakedelight.rating.dto.RatingResponse;
import com.cakedelight.rating.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Rating API", description = "Operations related to cake ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Operation(summary = "Create a rating")
    @PostMapping("/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingResponse createRating(@Valid @RequestBody RatingRequest request) {
        return ratingService.createRating(request);
    }

    @Operation(summary = "Get all ratings for a cake")
    @GetMapping("/cakes/{id}/ratings")
    public List<RatingResponse> getRatingsByCake(@PathVariable("id") Long cakeId) {
        return ratingService.getRatingsByCakeId(cakeId);
    }

    @Operation(summary = "Get average rating for a cake")
    @GetMapping("/cakes/{id}/ratings/average")
    public AverageRatingResponse getAverageRating(@PathVariable("id") Long cakeId) {
        return ratingService.getAverageRatingByCakeId(cakeId);
    }
}

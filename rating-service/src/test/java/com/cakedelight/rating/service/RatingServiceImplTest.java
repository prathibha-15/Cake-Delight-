package com.cakedelight.rating.service;

import com.cakedelight.rating.dto.AverageRatingResponse;
import com.cakedelight.rating.dto.RatingRequest;
import com.cakedelight.rating.dto.RatingResponse;
import com.cakedelight.rating.entity.Rating;
import com.cakedelight.rating.exception.ResourceNotFoundException;
import com.cakedelight.rating.mapper.RatingMapper;
import com.cakedelight.rating.repository.RatingRepository;
import com.cakedelight.rating.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private RatingMapper ratingMapper;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private Rating sampleRating;
    private RatingRequest sampleRequest;
    private RatingResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRating = new Rating();
        sampleRating.setId(1L);
        sampleRating.setCakeId(10L);
        sampleRating.setUserId(101L);
        sampleRating.setScore(5);
        sampleRating.setComment("Delicious!");
        sampleRating.setCreatedAt(LocalDateTime.now());

        sampleRequest = new RatingRequest(10L, 101L, 5, "Delicious!");
        sampleResponse = new RatingResponse(1L, 10L, 101L, 5, "Delicious!", LocalDateTime.now());
    }

    @Test
    void createRating_ShouldSaveAndReturnResponse() {
        when(ratingMapper.toEntity(sampleRequest)).thenReturn(sampleRating);
        when(ratingRepository.save(sampleRating)).thenReturn(sampleRating);
        when(ratingMapper.toResponse(sampleRating)).thenReturn(sampleResponse);

        RatingResponse response = ratingService.createRating(sampleRequest);

        assertNotNull(response);
        assertEquals(5, response.getScore());
        assertEquals("Delicious!", response.getComment());
        verify(ratingRepository).save(sampleRating);
    }

    @Test
    void getRatingsByCakeId_ShouldReturnList() {
        when(ratingRepository.findByCakeId(10L)).thenReturn(List.of(sampleRating));
        when(ratingMapper.toResponse(sampleRating)).thenReturn(sampleResponse);

        List<RatingResponse> ratings = ratingService.getRatingsByCakeId(10L);

        assertEquals(1, ratings.size());
        assertEquals(5, ratings.get(0).getScore());
    }

    @Test
    void getAverageRatingByCakeId_ExistingRatings_ShouldReturnAverage() {
        when(ratingRepository.findByCakeId(10L)).thenReturn(List.of(sampleRating));
        when(ratingRepository.findAverageScoreByCakeId(10L)).thenReturn(5.0);

        AverageRatingResponse response = ratingService.getAverageRatingByCakeId(10L);

        assertNotNull(response);
        assertEquals(10L, response.getCakeId());
        assertEquals(5.0, response.getAverage());
        assertEquals(1L, response.getCount());
    }

    @Test
    void getAverageRatingByCakeId_NoRatings_ShouldThrowException() {
        when(ratingRepository.findByCakeId(99L)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () ->
                ratingService.getAverageRatingByCakeId(99L));
    }
}

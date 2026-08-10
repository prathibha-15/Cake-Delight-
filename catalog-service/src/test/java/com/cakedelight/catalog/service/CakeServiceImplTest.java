package com.cakedelight.catalog.service;

import com.cakedelight.catalog.dto.CakeRequest;
import com.cakedelight.catalog.dto.CakeResponse;
import com.cakedelight.catalog.entity.Cake;
import com.cakedelight.catalog.exception.ResourceNotFoundException;
import com.cakedelight.catalog.mapper.CakeMapper;
import com.cakedelight.catalog.repository.CakeRepository;
import com.cakedelight.catalog.service.impl.CakeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CakeServiceImplTest {

    @Mock
    private CakeRepository cakeRepository;

    @Mock
    private CakeMapper cakeMapper;

    @InjectMocks
    private CakeServiceImpl cakeService;

    private Cake sampleCake;
    private CakeRequest sampleRequest;
    private CakeResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleCake = new Cake(1L, "Chocolate Truffle", "Rich chocolate cake", "Birthday", 799.0, 20, "http://example.com/cake.jpg");

        sampleRequest = new CakeRequest();
        sampleRequest.setName("Chocolate Truffle");
        sampleRequest.setDescription("Rich chocolate cake");
        sampleRequest.setCategory("Birthday");
        sampleRequest.setPrice(799.0);
        sampleRequest.setStock(20);
        sampleRequest.setImageUrl("http://example.com/cake.jpg");

        sampleResponse = new CakeResponse();
        sampleResponse.setId(1L);
        sampleResponse.setName("Chocolate Truffle");
        sampleResponse.setDescription("Rich chocolate cake");
        sampleResponse.setCategory("Birthday");
        sampleResponse.setPrice(799.0);
        sampleResponse.setStock(20);
        sampleResponse.setImageUrl("http://example.com/cake.jpg");
    }

    @Test
    void createCake_ShouldSaveAndReturnResponse() {
        when(cakeRepository.save(any(Cake.class))).thenReturn(sampleCake);
        when(cakeMapper.toResponse(sampleCake)).thenReturn(sampleResponse);

        CakeResponse response = cakeService.createCake(sampleRequest);

        assertNotNull(response);
        assertEquals("Chocolate Truffle", response.getName());
        verify(cakeRepository, times(1)).save(any(Cake.class));
    }

    @Test
    void getAllCakes_ShouldReturnList() {
        when(cakeRepository.findAll()).thenReturn(List.of(sampleCake));
        when(cakeMapper.toResponse(sampleCake)).thenReturn(sampleResponse);

        List<CakeResponse> cakes = cakeService.getAllCakes();

        assertEquals(1, cakes.size());
        assertEquals("Chocolate Truffle", cakes.get(0).getName());
    }

    @Test
    void getCakes_ByCategoryAndName_ShouldReturnFilteredList() {
        when(cakeRepository.findByCategoryAndNameContainingIgnoreCase("Birthday", "Truffle"))
                .thenReturn(List.of(sampleCake));
        when(cakeMapper.toResponse(sampleCake)).thenReturn(sampleResponse);

        List<CakeResponse> result = cakeService.getCakes("Birthday", "Truffle", null, null);

        assertEquals(1, result.size());
        verify(cakeRepository).findByCategoryAndNameContainingIgnoreCase("Birthday", "Truffle");
    }

    @Test
    void getCakes_ByCategoryOnly_ShouldReturnFilteredList() {
        when(cakeRepository.findByCategory("Birthday")).thenReturn(List.of(sampleCake));
        when(cakeMapper.toResponse(sampleCake)).thenReturn(sampleResponse);

        List<CakeResponse> result = cakeService.getCakes("Birthday", null, null, null);

        assertEquals(1, result.size());
        verify(cakeRepository).findByCategory("Birthday");
    }

    @Test
    void getCakes_ByNameOnly_ShouldReturnFilteredList() {
        when(cakeRepository.findByNameContainingIgnoreCase("Truffle")).thenReturn(List.of(sampleCake));
        when(cakeMapper.toResponse(sampleCake)).thenReturn(sampleResponse);

        List<CakeResponse> result = cakeService.getCakes(null, "Truffle", null, null);

        assertEquals(1, result.size());
        verify(cakeRepository).findByNameContainingIgnoreCase("Truffle");
    }

    @Test
    void getCakes_ByPriceRange_ShouldReturnFilteredList() {
        when(cakeRepository.findByPriceBetween(500.0, 1000.0)).thenReturn(List.of(sampleCake));
        when(cakeMapper.toResponse(sampleCake)).thenReturn(sampleResponse);

        List<CakeResponse> result = cakeService.getCakes(null, null, 500.0, 1000.0);

        assertEquals(1, result.size());
        verify(cakeRepository).findByPriceBetween(500.0, 1000.0);
    }

    @Test
    void getCakes_InvalidPriceRange_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                cakeService.getCakes(null, null, 1000.0, 500.0));
    }

    @Test
    void getCakeById_ExistingId_ShouldReturnCake() {
        when(cakeRepository.findById(1L)).thenReturn(Optional.of(sampleCake));
        when(cakeMapper.toResponse(sampleCake)).thenReturn(sampleResponse);

        CakeResponse response = cakeService.getCakeById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getCakeById_NonExistingId_ShouldThrowException() {
        when(cakeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cakeService.getCakeById(99L));
    }

    @Test
    void updateCake_ExistingId_ShouldUpdateAndReturnResponse() {
        when(cakeRepository.findById(1L)).thenReturn(Optional.of(sampleCake));
        when(cakeRepository.save(any(Cake.class))).thenReturn(sampleCake);
        when(cakeMapper.toResponse(sampleCake)).thenReturn(sampleResponse);

        CakeResponse response = cakeService.updateCake(1L, sampleRequest);

        assertNotNull(response);
        verify(cakeRepository).save(any(Cake.class));
    }

    @Test
    void deleteCake_ShouldCallRepository() {
        doNothing().when(cakeRepository).deleteById(1L);

        cakeService.deleteCake(1L);

        verify(cakeRepository, times(1)).deleteById(1L);
    }
}

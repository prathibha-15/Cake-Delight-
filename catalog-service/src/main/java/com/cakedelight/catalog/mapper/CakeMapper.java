package com.cakedelight.catalog.mapper;

import com.cakedelight.catalog.dto.CakeResponse;
import com.cakedelight.catalog.entity.Cake;
import org.springframework.stereotype.Component;

@Component
public class CakeMapper {

    public CakeResponse toResponse(Cake cake) {
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

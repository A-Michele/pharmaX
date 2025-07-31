package com.alaia.pharmX.mappers;

import org.springframework.stereotype.Component;
import com.alaia.pharmX.dtos.ProductDto;
import com.alaia.pharmX.models.Product;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) return null;

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setNationalCode(product.getNationalCode());
        dto.setCategory(product.getCategory());
        dto.setSupplierName(product.getSupplierName());

        return dto;
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) return null;

        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setNationalCode(dto.getNationalCode());
        product.setCategory(dto.getCategory());
        product.setSupplierName(dto.getSupplierName());

        return product;
    }
}

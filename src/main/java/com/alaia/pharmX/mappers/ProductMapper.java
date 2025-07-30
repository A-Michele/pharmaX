package com.alaia.pharmX.mappers;

import com.alaia.pharmX.dtos.ProductDto;
import com.alaia.pharmX.models.Product;

public class ProductMapper {

    public static ProductDto toDto(Product product) {
        if (product == null) return null;

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setNationalCode(product.getNationalCode());
        dto.setCategory(product.getCategory());
        dto.setSupplierName(product.getSupplierName());

        return dto;
    }

    public static Product toEntity(ProductDto dto) {
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

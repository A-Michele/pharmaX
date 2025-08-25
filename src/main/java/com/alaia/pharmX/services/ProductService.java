package com.alaia.pharmX.services;

import java.util.List;
import com.alaia.pharmX.dtos.ProductDto;

public interface ProductService {

	ProductDto saveProduct(ProductDto productDto);
	List<ProductDto> saveProducts(List<ProductDto> productDto);
	ProductDto getProductById(long id);
	List<ProductDto> getAllProducts();
	ProductDto updateProduct(ProductDto productDto);
	ProductDto deleteProduct(String nationalCode);
	ProductDto getProductByNationalCode(String nationalCode);
	ProductDto getProductByParam(Long id, String nationalCode);
	ProductDto patchSupplierNameToProductByNationalCode(String nationalCode, String supplierName);
	List<ProductDto> deleteAllProducts();
	List<ProductDto> getProductByCategory(String category);

	ProductDto deleteProductSafely(String nationalCode);

}
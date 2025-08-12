package com.alaia.pharmX.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alaia.pharmX.dtos.ProductDto;
import com.alaia.pharmX.dtos.SupplierNamePatchRequest;
import com.alaia.pharmX.services.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/product")
public class ProductController {

	@Autowired
	private ProductService productService;

	@GetMapping("/all")
	public ResponseEntity<List<ProductDto>> getAllProducts() {
		List<ProductDto> productDto = productService.getAllProducts();
		return new ResponseEntity<>(productDto, HttpStatus.OK);
	}

	@GetMapping("/product_id/{id}")
	public ResponseEntity<ProductDto> getProductById(@PathVariable int id) {
		ProductDto productDto =  productService.getProductById(id);
		return new ResponseEntity<>(productDto, HttpStatus.OK);
	}

	@GetMapping("/product_nationalCode/{nationalCode}")
	public ResponseEntity<ProductDto> getProductByNationalCode(@PathVariable String nationalCode) {
		ProductDto productDto =  productService.getProductByNationalCode(nationalCode);
		return new ResponseEntity<>(productDto, HttpStatus.OK);
	}

	@GetMapping()
	public ResponseEntity<ProductDto> getProductByParams(
			@RequestParam(name = "id", required = false) Long id,
			@RequestParam(name = "nationalCode", required = false) String nationalCode) {
		ProductDto productDto =  productService.getProductByParam(id, nationalCode);
		return new ResponseEntity<>(productDto, HttpStatus.CREATED);
	}

	@PostMapping("/add")
    public ResponseEntity<ProductDto> createCustomer(@Valid @RequestBody ProductDto productDto) {
		ProductDto createProductDto =  productService.saveProduct(productDto);
    	return new ResponseEntity<>(createProductDto, HttpStatus.CREATED);
    }

	@PutMapping("/update")
    public ResponseEntity<ProductDto> updateUser(@Valid @RequestBody ProductDto productDto) {
		ProductDto updateProductDto =  productService.updateProduct(productDto);
    	return new ResponseEntity<>(updateProductDto, HttpStatus.OK);
    }

	@PatchMapping("/updateSupplierName")
    public ResponseEntity<ProductDto> patchSupplierNameToProduct(@RequestParam(name = "nationalCode") String nationalCode, @Valid @RequestBody SupplierNamePatchRequest supplierNamePatchRequestDto) {
		ProductDto updateProductDto =  productService.patchSupplierNameToProductByNationalCode(nationalCode, supplierNamePatchRequestDto.getSupplierName());
        return new ResponseEntity<>(updateProductDto, HttpStatus.OK);
    }

	@DeleteMapping("/delete")
    public ResponseEntity<ProductDto> deleteProductDto(@RequestParam(name = "nationalCode") String nationalCode) {
		ProductDto productDto =  productService.deleteProduct(nationalCode);
        return new ResponseEntity<>(productDto, HttpStatus.OK);
    }

	@PostMapping("/adds")
	public ResponseEntity<List<ProductDto>> saveProducts(@RequestBody List<ProductDto> productDtos) {
		List<ProductDto> saved = productService.saveProducts(productDtos);
		return new ResponseEntity<>(saved, HttpStatus.OK);
	}

	@DeleteMapping("/deleteAll")
	public ResponseEntity<List<ProductDto>> deleteAll() {
		List<ProductDto> deleted = productService.deleteAllProducts();
		return new ResponseEntity<>(deleted, HttpStatus.OK);
	}
}

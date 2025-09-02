package com.alaia.pharmX.servicesImpl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alaia.pharmX.dtos.ProductDto;
import com.alaia.pharmX.exceptions.servicesImpl.CannotDeleteProductWithOpenOrdersException;
import com.alaia.pharmX.exceptions.servicesImpl.CategoryNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.mappers.ProductMapper;
import com.alaia.pharmX.models.Product;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.OrderLine;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.SectionRepository;
import com.alaia.pharmX.repositories.order.OrderLineRepository;
import com.alaia.pharmX.services.ProductService;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImp implements ProductService{

	@Autowired
	private ProductRepository productRepository;

	@Autowired
    private ProductMapper productMapper;

	@Autowired
	private SectionRepository sectionRepository;

	@Autowired
	private OrderLineRepository orderLineRepository;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ProductDto saveProduct(ProductDto productDto) {

		if(productRepository.existsByNationalCode(productDto.getNationalCode())) {
			throw new ProductAlreadyExistsException("Product already exists with national code : " + productDto.getNationalCode());
		}

		if(!sectionRepository.existsByCategory( productDto.getCategory() ) ){
			throw new  CategoryNotFoundException("Category not found: " + productDto.getCategory());
		}

		Product product = productMapper.toEntity(productDto);
		Product productSaved = productRepository.save(product);

		return productMapper.toDto(productSaved);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ProductDto getProductById(long id) {

		Product product = productRepository.findById(id).orElseThrow(
        		()-> new ProductNotFoundException("Product not found with ID : " + id));
		return productMapper.toDto(product);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<ProductDto> getAllProducts() {

		List<Product> product = productRepository.findAll();
		return product.stream()
				.map(productMapper::toDto)
				.toList();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ProductDto updateProduct(ProductDto productDto) {

		Product existingProduct = productRepository.findByNationalCode(productDto.getNationalCode());
		if(existingProduct == null ) throw new ProductNotFoundException("Product not found with nationa code : " + productDto.getNationalCode());

		existingProduct.setName(productDto.getName());
		existingProduct.setCategory(productDto.getCategory());
		existingProduct.setSupplierName(productDto.getSupplierName());
		existingProduct.setNationalCode(productDto.getNationalCode());

		Product updatedProduct = productRepository.save(existingProduct);
		return productMapper.toDto(updatedProduct);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ProductDto deleteProduct(String nationalCode) {

		Product product = productRepository.findByNationalCode(nationalCode);
	    if (product == null) {
	        throw new ProductNotFoundException("Product not found with nationalCode : " + nationalCode);
	    }

	    productRepository.delete(product);
	    return productMapper.toDto(product);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ProductDto getProductByNationalCode(String nationalCode) {

		Product product = productRepository.findByNationalCode(nationalCode);
		if(product == null ) throw new ProductNotFoundException("Product not found with nationalCode : " + nationalCode);
		return productMapper.toDto(product);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ProductDto getProductByParam(Long id, String nationalCode) {

		if(id != null) return this.getProductById(id);
		else if(nationalCode != null) return this.getProductByNationalCode(nationalCode);
		return null;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ProductDto patchSupplierNameToProductByNationalCode(String nationalCode, String supplierName) {

		if (nationalCode == null || nationalCode.isBlank()) {
            throw new IllegalArgumentException("nationalCode non può essere vuoto");
        }
        if (supplierName == null || supplierName.isBlank()) {
            throw new IllegalArgumentException("supplierName non può essere vuoto");
        }

        Product product = productRepository.findByNationalCode(nationalCode);
        if (product == null) {
            throw new ProductNotFoundException("Product not found with nationalCode : " + nationalCode);
        }

        product.setSupplierName(supplierName);

        Product updated = productRepository.save(product);
        return productMapper.toDto(updated);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<ProductDto> saveProducts(List<ProductDto> productDtos) {

		if (productDtos == null || productDtos.isEmpty()) {
	        throw new IllegalArgumentException("La lista di prodotti non può essere vuota o nulla");
	    }

	    for (ProductDto dto : productDtos) {
	        if (productRepository.existsByNationalCode(dto.getNationalCode())) {
	            throw new ProductAlreadyExistsException(
	                "Product already exists with national code : " + dto.getNationalCode()
	            );
	        }
	        if(!sectionRepository.existsByCategory( dto.getCategory() ) ){
				throw new  CategoryNotFoundException("Category not found: " + dto.getCategory());
			}
	    }

	    List<Product> products = productDtos.stream()
	            .map(productMapper::toEntity)
	            .toList();

	    List<Product> savedProducts = productRepository.saveAll(products);

	    return savedProducts.stream()
	            .map(productMapper::toDto)
	            .toList();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<ProductDto> deleteAllProducts() {

		List<Product> allProducts = productRepository.findAll();

	    if (allProducts.isEmpty()) {
	        throw new ProductNotFoundException("No products found to delete");
	    }

	    productRepository.deleteAll();

	    return allProducts.stream()
	            .map(productMapper::toDto)
	            .toList();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<ProductDto> getProductByCategory(String category) {

		List<Product> products = productRepository.findByCategory(category);
		if(products == null || products.isEmpty())
			throw new ProductNotFoundException("No products found with category: " + category);
		return products.stream()
				.map(productMapper::toDto)
				.toList();
	}

	@Override
	@Transactional
	public ProductDto deleteProductSafely(String nationalCode) {

		Product product = productRepository.findByNationalCode(nationalCode);
	    if (product == null) {
	        throw new ProductNotFoundException("Product not found with nationalCode: " + nationalCode);
	    }

	    List<OrderLine> orderLines = orderLineRepository.findByNationalCode(nationalCode);

	    if (orderLines == null || orderLines.isEmpty()) {
	        productRepository.delete(product);
	        return productMapper.toDto(product);
	    }

	    Order orderException=null;
	    boolean hasOpenOrders = false;
	    for (OrderLine ol : orderLines) {
	        Order order = ol.getOrder();
	        State s = order.getState();
	        if (s != State.CANCELED && s != State.COMPLETED) {
	        	orderException = order;
	            hasOpenOrders = true;
	            break;
	        }
	    }

	    if (hasOpenOrders) {
	        throw new CannotDeleteProductWithOpenOrdersException(
	            "Cannot delete product with nationalCode: " + nationalCode +
	            ". Product present in the order: " + orderException.getCode() +
	            ", with state: " + orderException.getState()
	        );
	    }

	    productRepository.delete(product);
	    return productMapper.toDto(product);
	}
}
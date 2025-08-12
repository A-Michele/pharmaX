package com.alaia.pharmX.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alaia.pharmX.dtos.ProductDto;
import com.alaia.pharmX.mappers.ProductMapper;
import com.alaia.pharmX.models.Product;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.servicesImpl.ProductServiceImp;
import com.alaia.pharmX.servicesImpl.exceptions.ProductAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.ProductNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductMapper productMapper;

	@InjectMocks
	private ProductServiceImp productService;

	private ProductDto productDto;
    private Product product;
    private Product savedProduct;
    private ProductDto savedProductDto;

	@BeforeEach
    void setUp() {
        productDto = new ProductDto(1L, "Prod A", "NC123", "CAT1", "Supplier X");

        product = new Product(1L, "Prod A", "NC123", "CAT1", "Supplier X");

        // simuliamo che il DB generi un ID differente al salvataggio
        savedProduct = new Product(10L, "Prod A", "NC123", "CAT1", "Supplier X");
        savedProductDto = new ProductDto(10L, "Prod A", "NC123", "CAT1", "Supplier X");
    }

    // -----------> SAVE PRODUCT <-----------

    @Test
    void saveProduct_ShouldReturnProductDto_WhenProductDoesNotExist() {
        // Arrange
        when(productRepository.existsByNationalCode(anyString())).thenReturn(false);
        when(productMapper.toEntity(any(ProductDto.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(savedProductDto);

        // Act
        ProductDto result = productService.saveProduct(productDto);

        // Assert
        assertNotNull(result);
        assertEquals("NC123", result.getNationalCode());
        assertEquals(10L, result.getId());
        verify(productRepository).existsByNationalCode(productDto.getNationalCode());
        verify(productRepository).save(product);
        verify(productMapper).toDto(savedProduct);
    }

    @Test
    void saveProduct_ShouldThrowException_WhenProductAlreadyExists() {
        // Arrange
        when(productRepository.existsByNationalCode(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ProductAlreadyExistsException.class, () -> productService.saveProduct(productDto));
        verify(productRepository).existsByNationalCode(productDto.getNationalCode());
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    // -----------> GET PRODUCT BY ID <-----------

    @Test
    void getProductById_ShouldReturnProductDto_WhenProductExists() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(savedProduct));
        when(productMapper.toDto(any(Product.class))).thenReturn(savedProductDto);

        // Act
        ProductDto result = productService.getProductById(10L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(productRepository).findById(10L);
        verify(productMapper).toDto(savedProduct);
    }

    @Test
    void getProductById_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(1L));
        verify(productRepository).findById(1L);
        verifyNoInteractions(productMapper);
    }

    // -----------> GET ALL PRODUCTS <-----------

    @Test
    void getAllProducts_ShouldReturnProductList_WhenProductsExist() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(savedProduct));
        when(productMapper.toDto(any(Product.class))).thenReturn(savedProductDto);

        // Act
        List<ProductDto> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("NC123", result.get(0).getNationalCode());
        verify(productRepository).findAll();
        verify(productMapper).toDto(savedProduct);
    }

    // -----------> UPDATE PRODUCT <-----------

    @Test
    void updateProduct_ShouldReturnUpdatedProductDto_WhenProductExists() {
        // Arrange
        Product existing = new Product(5L, "Old", "NC123", "OLD_CAT", "OldSup");
        Product updated = new Product(5L, "Prod A", "NC123", "CAT1", "Supplier X");
        ProductDto updatedDto = new ProductDto(5L, "Prod A", "NC123", "CAT1", "Supplier X");

        when(productRepository.findByNationalCode(anyString())).thenReturn(existing);
        when(productRepository.save(any(Product.class))).thenReturn(updated);
        when(productMapper.toDto(any(Product.class))).thenReturn(updatedDto);

        // Act
        ProductDto result = productService.updateProduct(productDto);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Prod A", result.getName());
        assertEquals("CAT1", result.getCategory());
        assertEquals("Supplier X", result.getSupplierName());
        verify(productRepository).findByNationalCode(productDto.getNationalCode());
        verify(productRepository).save(existing);
        verify(productMapper).toDto(updated);
    }

    @Test
    void updateCustomer_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        when(productRepository.findByNationalCode(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(productDto));
        verify(productRepository).findByNationalCode(productDto.getNationalCode());
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    // -----------> DELETE PRODUCT <-----------

    @Test
    void deleteProduct_ShouldReturnProductDto_WhenProductExists() {
        // Arrange
        when(productRepository.findByNationalCode(anyString())).thenReturn(savedProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(savedProductDto);

        // Act
        ProductDto deleted = productService.deleteProduct("NC123");

        // Assert
        assertNotNull(deleted);
        assertEquals("NC123", deleted.getNationalCode());
        verify(productRepository).findByNationalCode("NC123");
        verify(productRepository).delete(savedProduct);
        verify(productMapper).toDto(savedProduct);
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        when(productRepository.findByNationalCode(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct("NC404"));
        verify(productRepository).findByNationalCode("NC404");
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    // -----------> GET BY NATIONAL CODE <-----------

    @Test
    void getProductByNationalCode_ShouldReturnProductDto_WhenPresent() {
        // Arrange
        when(productRepository.findByNationalCode(anyString())).thenReturn(savedProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(savedProductDto);

        // Act
        ProductDto result = productService.getProductByNationalCode("NC123");

        // Assert
        assertNotNull(result);
        assertEquals("NC123", result.getNationalCode());
        verify(productRepository).findByNationalCode("NC123");
        verify(productMapper).toDto(savedProduct);
    }

    @Test
    void getProductByNationalCode_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(productRepository.findByNationalCode(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getProductByNationalCode("NC404"));
        verify(productRepository).findByNationalCode("NC404");
        verifyNoInteractions(productMapper);
    }

    // -----------> GET BY PARAM <-----------

    @Test
    void getProductByParam_ShouldPreferId_WhenIdPresent() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(savedProduct));
        when(productMapper.toDto(any(Product.class))).thenReturn(savedProductDto);

        // Act
        ProductDto result = productService.getProductByParam(10L, "IGNORED");

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(productRepository).findById(10L);
        verify(productMapper).toDto(savedProduct);
    }

    @Test
    void getProductByParam_ShouldUseNationalCode_WhenIdIsNull() {
        // Arrange
        when(productRepository.findByNationalCode(anyString())).thenReturn(savedProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(savedProductDto);

        // Act
        ProductDto result = productService.getProductByParam(null, "NC123");

        // Assert
        assertNotNull(result);
        assertEquals("NC123", result.getNationalCode());
        verify(productRepository).findByNationalCode("NC123");
        verify(productMapper).toDto(savedProduct);
    }

    @Test
    void getProductByParam_ShouldReturnNull_WhenBothNull() {
        // Act
        ProductDto result = productService.getProductByParam(null, null);

        // Assert
        assertNull(result);
        verifyNoInteractions(productRepository, productMapper);
    }

    // -----------> PATCH SUPPLIER NAME <-----------

    @Test
    void patchSupplierName_ShouldUpdate_WhenValidInput() {
        // Arrange
        Product found = new Product(7L, "P", "NC7", "C", "OldSup");
        Product updated = new Product(7L, "P", "NC7", "C", "NewSup");
        ProductDto updatedDto = new ProductDto(7L, "P", "NC7", "C", "NewSup");

        when(productRepository.findByNationalCode(anyString())).thenReturn(found);
        when(productRepository.save(any(Product.class))).thenReturn(updated);
        when(productMapper.toDto(any(Product.class))).thenReturn(updatedDto);

        // Act
        ProductDto result = productService.patchSupplierNameToProductByNationalCode("NC7", "NewSup");

        // Assert
        assertNotNull(result);
        assertEquals("NewSup", result.getSupplierName());
        verify(productRepository).findByNationalCode("NC7");
        verify(productRepository).save(found);
        verify(productMapper).toDto(updated);
    }

    @Test
    void patchSupplierName_ShouldThrow_WhenNationalCodeBlank() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> productService.patchSupplierNameToProductByNationalCode("   ", "S"));
        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void patchSupplierName_ShouldThrow_WhenSupplierNameBlank() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> productService.patchSupplierNameToProductByNationalCode("NC7", ""));
        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void patchSupplierName_ShouldThrow_WhenProductNotFound() {
        // Arrange
        when(productRepository.findByNationalCode(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                () -> productService.patchSupplierNameToProductByNationalCode("NC7", "NewSup"));
        verify(productRepository).findByNationalCode("NC7");
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    // -----------> SAVE PRODUCTS <-----------

    @Test
    void saveProducts_ShouldReturnList_WhenAllNew() {
        // Arrange
        ProductDto d1 = new ProductDto(0L, "P1", "NC1", "C1", "S1");
        ProductDto d2 = new ProductDto(0L, "P2", "NC2", "C2", "S2");
        Product e1 = new Product(0L, "P1", "NC1", "C1", "S1");
        Product e2 = new Product(0L, "P2", "NC2", "C2", "S2");
        Product s1 = new Product(101L, "P1", "NC1", "C1", "S1");
        Product s2 = new Product(102L, "P2", "NC2", "C2", "S2");
        ProductDto sd1 = new ProductDto(101L, "P1", "NC1", "C1", "S1");
        ProductDto sd2 = new ProductDto(102L, "P2", "NC2", "C2", "S2");

        when(productRepository.existsByNationalCode("NC1")).thenReturn(false);
        when(productRepository.existsByNationalCode("NC2")).thenReturn(false);

        when(productMapper.toEntity(d1)).thenReturn(e1);
        when(productMapper.toEntity(d2)).thenReturn(e2);

        when(productRepository.saveAll(List.of(e1, e2))).thenReturn(List.of(s1, s2));

        when(productMapper.toDto(s1)).thenReturn(sd1);
        when(productMapper.toDto(s2)).thenReturn(sd2);

        // Act
        List<ProductDto> result = productService.saveProducts(List.of(d1, d2));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("NC1", result.get(0).getNationalCode());
        assertEquals("NC2", result.get(1).getNationalCode());
        verify(productRepository).existsByNationalCode("NC1");
        verify(productRepository).existsByNationalCode("NC2");
        verify(productMapper).toEntity(d1);
        verify(productMapper).toEntity(d2);
        verify(productRepository).saveAll(List.of(e1, e2));
        verify(productMapper).toDto(s1);
        verify(productMapper).toDto(s2);
    }

    @Test
    void saveProducts_ShouldThrow_WhenAnyNationalCodeExists() {
        // Arrange
        ProductDto d1 = new ProductDto(0L, "P1", "NC1", "C1", "S1");
        ProductDto d2 = new ProductDto(0L, "P2", "NC2", "C2", "S2");

        when(productRepository.existsByNationalCode("NC1")).thenReturn(false);
        when(productRepository.existsByNationalCode("NC2")).thenReturn(true);

        // Act & Assert
        assertThrows(ProductAlreadyExistsException.class, () -> productService.saveProducts(List.of(d1, d2)));
        verify(productRepository).existsByNationalCode("NC1");
        verify(productRepository).existsByNationalCode("NC2");
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productMapper);
    }

    @Test
    void saveProducts_ShouldThrow_WhenEmptyList() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> productService.saveProducts(List.of()));
        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void saveProducts_ShouldThrow_WhenNullList() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> productService.saveProducts(null));
        verifyNoInteractions(productRepository, productMapper);
    }

    // -----------> DELETE ALL PRODUCTS <-----------

    @Test
    void deleteAllProducts_ShouldReturnDeletedList_WhenThereAreProducts() {
    	// Arrange
    	Product p1 = new Product(101L, "P1", "NC1", "C1", "S1");
    	Product p2 = new Product(102L, "P2", "NC2", "C2", "S2");
    	ProductDto d1 = new ProductDto(101L, "P1", "NC1", "C1", "S1");
    	ProductDto d2 = new ProductDto(102L, "P2", "NC2", "C2", "S2");

    	when(productRepository.findAll()).thenReturn(List.of(p1, p2));
    	when(productMapper.toDto(p1)).thenReturn(d1);
    	when(productMapper.toDto(p2)).thenReturn(d2);

    	// Act
    	List<ProductDto> deleted = productService.deleteAllProducts();

    	// Assert
    	assertNotNull(deleted);
    	assertEquals(2, deleted.size());
    	assertEquals("NC1", deleted.get(0).getNationalCode());
    	assertEquals("NC2", deleted.get(1).getNationalCode());
    	verify(productRepository).findAll();
    	verify(productRepository).deleteAll();
    	verify(productMapper).toDto(p1);
    	verify(productMapper).toDto(p2);
    }

    @Test
    void deleteAllProducts_ShouldThrow_WhenNoProducts() {
    	// Arrange
    	when(productRepository.findAll()).thenReturn(List.of());

    	// Act & Assert
    	assertThrows(ProductNotFoundException.class, () -> productService.deleteAllProducts());
    	verify(productRepository).findAll();
    	verifyNoMoreInteractions(productRepository);
    	verifyNoInteractions(productMapper);
    }
}

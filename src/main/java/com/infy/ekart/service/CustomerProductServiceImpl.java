package com.infy.ekart.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infy.ekart.dto.ProductDTO;
import com.infy.ekart.entity.Product;
import com.infy.ekart.exception.EKartException;
import com.infy.ekart.repository.ProductRepository;

//add the missing annotations
@Service(value = "customerProductService")
public class CustomerProductServiceImpl implements CustomerProductService {
	@Autowired
	private ProductRepository productRepository;

	
	// Get all the product details from the database
    // And return the same
	
	@Override
	public List<ProductDTO> getAllProducts() throws EKartException {
		List<ProductDTO> productDTOList = new ArrayList<>();
		
		Iterable<Product> productList = productRepository.findAll();
		
		productList.forEach((product) -> {
			ProductDTO productDTO = new ProductDTO();
			productDTO.setProductId(product.getProductId());
			productDTO.setName(product.getName());
			productDTO.setDescription(product.getDescription());
			productDTO.setCategory(product.getCategory());
			productDTO.setBrand(product.getBrand());
			productDTO.setPrice(product.getPrice());
			productDTO.setAvailableQuantity(product.getAvailableQuantity());
			
			productDTOList.add(productDTO);
		});
		
		return productDTOList;
		
	}

	@Override
	public ProductDTO getProductById(Integer productId) throws EKartException {

		Optional<Product> productOp = productRepository.findById(productId);
		Product product = productOp
				.orElseThrow(() -> new EKartException("ProductService.PRODUCT_NOT_AVAILABLE"));

		ProductDTO productDTO = new ProductDTO();
		productDTO.setBrand(product.getBrand());
		productDTO.setCategory(product.getCategory());
		productDTO.setDescription(product.getDescription());
		productDTO.setName(product.getName());
		productDTO.setPrice(product.getPrice());
		productDTO.setProductId(product.getProductId());
		productDTO.setAvailableQuantity(product.getAvailableQuantity());

		return productDTO;
	}

	@Override
	public void reduceAvailableQuantity(Integer productId, Integer quantity) throws EKartException {
		Optional<Product> productOp = productRepository.findById(productId);
		Product product = productOp
				.orElseThrow(() -> new EKartException("ProductService.PRODUCT_NOT_AVAILABLE"));
		product.setAvailableQuantity(product.getAvailableQuantity() - quantity);
	}
}

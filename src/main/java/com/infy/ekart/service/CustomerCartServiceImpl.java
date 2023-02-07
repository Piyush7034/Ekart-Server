package com.infy.ekart.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infy.ekart.dto.CartProductDTO;
import com.infy.ekart.dto.CustomerCartDTO;
import com.infy.ekart.dto.ProductDTO;
import com.infy.ekart.entity.CartProduct;
import com.infy.ekart.entity.CustomerCart;
import com.infy.ekart.entity.Product;
import com.infy.ekart.exception.EKartException;
import com.infy.ekart.repository.CartProductRepository;
import com.infy.ekart.repository.CustomerCartRepository;
import com.infy.ekart.repository.ProductRepository;

//add the missing annotations
@Service(value = "customerCartService")
public class CustomerCartServiceImpl implements CustomerCartService {
	@Autowired
	private CustomerCartRepository customerCartRepository;

	@Autowired
	private CartProductRepository cartProductRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CustomerProductService customerProductService;

	@Override
	public Integer addProductToCart(CustomerCartDTO customerCartDTO) throws EKartException {
		Set<CartProduct> cartProducts = new HashSet<>();
		Integer cartId = null;
		for (CartProductDTO cartProductDTO : customerCartDTO.getCartProducts()) {
			CartProduct cartProduct = new CartProduct();
			cartProduct.setProductId(cartProductDTO.getProduct().getProductId());
			cartProduct.setQuantity(cartProductDTO.getQuantity());
			cartProducts.add(cartProduct);
		}
		Optional<CustomerCart> cartOptional = customerCartRepository
				.findByCustomerEmailId(customerCartDTO.getCustomerEmailId());
		if (cartOptional.isEmpty()) {
			CustomerCart newCart = new CustomerCart();
			newCart.setCustomerEmailId(customerCartDTO.getCustomerEmailId());
			newCart.setCartProducts(cartProducts);
			customerCartRepository.save(newCart);
			cartId = newCart.getCartId();
		} else {
			CustomerCart cart = cartOptional.get();
			for (CartProduct cartProductToBeAdded : cartProducts) {
				Boolean found = false;
				for (CartProduct cartProductFromCart : cart.getCartProducts()) {
					if (cartProductFromCart.equals(cartProductToBeAdded)) {

						cartProductFromCart
								.setQuantity(cartProductToBeAdded.getQuantity() + cartProductFromCart.getQuantity());
						found = true;
					}
				}
				if (found == false) {
					cart.getCartProducts().add(cartProductToBeAdded);
				}
			}

			cartId = cart.getCartId();
		}
		return cartId;
	}

	
	//Get the customer cart details by using customerEmailId
	//If no cart found then throw an EKartException with message "CustomerCartService.NO_CART_FOUND"
	//Else if cart is empty then throw an EKartException with message "CustomerCartService.NO_PRODUCT_ADDED_TO_CART"
	//Otherwise return the set of cart products
	
	@Override
	public Set<CartProductDTO> getProductsFromCart(String customerEmailId) throws EKartException {
		Set<CartProductDTO> cartProductDTOList = new HashSet<>();
		Optional<CustomerCart> optionalCustomerCart = customerCartRepository.findByCustomerEmailId(customerEmailId);
		CustomerCart customerCart = optionalCustomerCart.
									orElseThrow(() -> new EKartException("CustomerCartService.NO_CART_FOUND"));
		
		if(customerCart.getCartProducts().isEmpty()) {
			throw new EKartException("CustomerCartService.NO_PRODUCT_ADDED_TO_CART");
		}
		
		for(CartProduct cartProduct: customerCart.getCartProducts()) {
			CartProductDTO cartProductDTO = new CartProductDTO();
			cartProductDTO.setCartProductId(cartProduct.getCartProductId());
			
//			Optional<Product> productOp = productRepository.findById(cartProduct.getProductId());
//			Product product = productOp
//					.orElseThrow(() -> new EKartException("ProductService.PRODUCT_NOT_AVAILABLE"));
//
//			ProductDTO productDTO = new ProductDTO();
//			productDTO.setBrand(product.getBrand());
//			productDTO.setCategory(product.getCategory());
//			productDTO.setDescription(product.getDescription());
//			productDTO.setName(product.getName());
//			productDTO.setPrice(product.getPrice());
//			productDTO.setProductId(product.getProductId());
//			productDTO.setAvailableQuantity(product.getAvailableQuantity());
			
			ProductDTO productDTO = customerProductService.getProductById(cartProduct.getProductId());
			
			cartProductDTO.setProduct(productDTO);
			cartProductDTO.setQuantity(cartProduct.getQuantity());
			
			cartProductDTOList.add(cartProductDTO);
		}
		
		return cartProductDTOList;
		
	}

	
	//Get the customer cart details by using customerEmailId
	//If no cart found then throw an EKartException with message "CustomerCartService.NO_CART_FOUND"
	//Else if cart is empty then throw an EKartException with message "CustomerCartService.NO_PRODUCT_ADDED_TO_CART"
	//Otherwise delete the given product from the customer cart
	
	@Override
	public void deleteProductFromCart(String customerEmailId, Integer productId) throws EKartException {
		Optional<CustomerCart> optionalCustomerCart = customerCartRepository.findByCustomerEmailId(customerEmailId);
		CustomerCart customerCart = optionalCustomerCart.
									orElseThrow(() -> new EKartException("CustomerCartService.NO_CART_FOUND"));
		
		if(customerCart.getCartProducts().isEmpty()) {
			throw new EKartException("CustomerCartService.NO_PRODUCT_ADDED_TO_CART");
		}
		
		for(CartProduct cartProduct: customerCart.getCartProducts()) {
			if(cartProduct.getProductId() == productId) {
				customerCart.getCartProducts().remove(cartProduct);
			}
		}
		
		cartProductRepository.deleteById(productId);
		
	}

	@Override
	public void deleteAllProductsFromCart(String customerEmailId) throws EKartException {
		Optional<CustomerCart> cartOptional = customerCartRepository.findByCustomerEmailId(customerEmailId);
		CustomerCart cart = cartOptional.orElseThrow(() -> new EKartException("CustomerCartService.NO_CART_FOUND"));

		if (cart.getCartProducts().isEmpty()) {
			throw new EKartException("CustomerCartService.NO_PRODUCT_ADDED_TO_CART");
		}
		List<Integer> productIds = new ArrayList<>();
		cart.getCartProducts().parallelStream().forEach(cp -> {
			productIds.add(cp.getCartProductId());
			cart.getCartProducts().remove(cp);
		});

		productIds.forEach(pid -> {

			cartProductRepository.deleteById(pid);
		});

	}

	@Override
	public void modifyQuantityOfProductInCart(String customerEmailId, Integer productId, Integer quantity)
			throws EKartException {

		Optional<CustomerCart> cartOptional = customerCartRepository.findByCustomerEmailId(customerEmailId);
		CustomerCart cart = cartOptional.orElseThrow(() -> new EKartException("CustomerCartService.NO_CART_FOUND"));

		if (cart.getCartProducts().isEmpty()) {
			throw new EKartException("CustomerCartService.NO_PRODUCT_ADDED_TO_CART");
		}
		CartProduct selectedProduct = null;
		for (CartProduct product : cart.getCartProducts()) {
			if (product.getProductId().equals(productId)) {
				selectedProduct = product;
			}
		}
		if (selectedProduct == null) {
			throw new EKartException("CustomerCartService.PRODUCT_ALREADY_NOT_AVAILABLE");
		}
		selectedProduct.setQuantity(quantity);
	}

}
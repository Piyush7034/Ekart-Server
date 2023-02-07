package com.infy.ekart.repository;

import java.util.List;

import com.infy.ekart.entity.Order;

public interface OrderRepository {
  
	// add methods if required
	List<Order> findByCustomerEmailId(String customerEmailId);
	
}

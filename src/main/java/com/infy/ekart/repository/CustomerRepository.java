package com.infy.ekart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.infy.ekart.entity.Customer;

public interface CustomerRepository extends CrudRepository<Customer, String> {
	List<Customer> findByPhoneNumber(String phoneNumber);

}

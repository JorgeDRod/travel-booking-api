package com.jorgedrod.travelbooking.repository;

import com.jorgedrod.travelbooking.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}

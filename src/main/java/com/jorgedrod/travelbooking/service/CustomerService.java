package com.jorgedrod.travelbooking.service;

import org.springframework.stereotype.Service;
import com.jorgedrod.travelbooking.repository.CustomerRepository;
import com.jorgedrod.travelbooking.entity.Customer;
import java.util.List;
import java.util.Optional;


@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer existingCustomer = customerRepository.findById(id).orElse(null);
        if (existingCustomer == null) {
            return null;
        }
        existingCustomer.setFirstName(updatedCustomer.getFirstName());
        existingCustomer.setLastName(updatedCustomer.getLastName());
        existingCustomer.setPhone(updatedCustomer.getPhone());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        return customerRepository.save(existingCustomer);
    }

    public boolean deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            return false;
        }
        customerRepository.deleteById(id);
        return true;
    }
}

package com.app.digitalbanking.repositories;

import com.app.digitalbanking.entities.Customer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer,Long>{    
    
}

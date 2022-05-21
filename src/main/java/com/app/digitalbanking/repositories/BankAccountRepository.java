package com.app.digitalbanking.repositories;

import com.app.digitalbanking.entities.BankAccount;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount,String>{
    
}

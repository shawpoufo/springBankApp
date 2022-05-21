package com.app.digitalbanking.services;

import java.util.List;

import com.app.digitalbanking.dtos.AccountHistoryDTO;
import com.app.digitalbanking.dtos.AccountOperationDTO;
import com.app.digitalbanking.dtos.BankAccountDTO;
import com.app.digitalbanking.dtos.CurrentBankAccountDTO;
import com.app.digitalbanking.dtos.CustomerDTO;
import com.app.digitalbanking.dtos.SavingBankAccountDTO;
import com.app.digitalbanking.exceptions.BalanceNotSufficientException;
import com.app.digitalbanking.exceptions.BankAccountNotFoundException;
import com.app.digitalbanking.exceptions.CustomerNotFoundException;

public interface BankAccountService {
    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;
    SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;
    List<CustomerDTO> listCustomers();
    BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;
    void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;
    void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException;
    List<BankAccountDTO> bankAccountList();
    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;
    CustomerDTO updateCustomer(CustomerDTO customerDTO);
    void deleteCustomer(Long customerId);
    List<AccountOperationDTO> accountHistory(String accountId);
    AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException;
}

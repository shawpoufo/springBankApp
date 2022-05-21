package com.app.digitalbanking.services;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.app.digitalbanking.dtos.AccountHistoryDTO;
import com.app.digitalbanking.dtos.AccountOperationDTO;
import com.app.digitalbanking.dtos.BankAccountDTO;
import com.app.digitalbanking.dtos.CurrentBankAccountDTO;
import com.app.digitalbanking.dtos.CustomerDTO;
import com.app.digitalbanking.dtos.SavingBankAccountDTO;
import com.app.digitalbanking.entities.AccountOperation;
import com.app.digitalbanking.entities.BankAccount;
import com.app.digitalbanking.entities.CurrentAccount;
import com.app.digitalbanking.entities.Customer;
import com.app.digitalbanking.entities.SavingAccount;
import com.app.digitalbanking.enums.OperationType;
import com.app.digitalbanking.exceptions.BalanceNotSufficientException;
import com.app.digitalbanking.exceptions.BankAccountNotFoundException;
import com.app.digitalbanking.exceptions.CustomerNotFoundException;
import com.app.digitalbanking.mappers.BankAccountMapperImpl;
import com.app.digitalbanking.repositories.AccountOperationRepository;
import com.app.digitalbanking.repositories.BankAccountRepository;
import com.app.digitalbanking.repositories.CustomerRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl dtoMapper;
    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Savingnew Customer");
        Customer customer= dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer= customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }
    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId)
            throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null)
            throw new CustomerNotFoundException("Customer not found");
        CurrentAccount currentAccount=new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);        currentAccount.setCustomer(customer);
        CurrentAccount savedBankAccount= bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }
    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId)
            throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null)
        throw new CustomerNotFoundException("Customer not found");
        SavingAccount savingAccount=new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);
        SavingAccount savedBankAccount= bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }
    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers= customerRepository.findAll();
        List<CustomerDTO> customerDTOS= customers.stream()
        .map(customer-> dtoMapper.fromCustomer(customer))
        .collect(Collectors.toList());
        return customerDTOS;
    }
    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
            .orElseThrow(()->new BankAccountNotFoundException("BankAccountnot found"));
            if(bankAccount instanceof SavingAccount){
                SavingAccount savingAccount= (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);
            } 
            else{
                CurrentAccount currentAccount= (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }
    }
    @Override
    public void debit(String accountId, double amount, String description)
            throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
            .orElseThrow(()->new BankAccountNotFoundException("BankAccountnot found"));
            if(bankAccount.getBalance()<amount)
                throw new BalanceNotSufficientException("Balance not sufficient");
            AccountOperation accountOperation=new AccountOperation();
            accountOperation.setType(OperationType.DEBIT);
            accountOperation.setAmount(amount);        accountOperation.setDescription(description);
            accountOperation.setOperationDate(new Date());        accountOperation.setBankAccount(bankAccount);
            accountOperationRepository.save(accountOperation);        bankAccount.setBalance(bankAccount.getBalance()-amount);
            bankAccountRepository.save(bankAccount);
        
    }
    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
            .orElseThrow(()->new BankAccountNotFoundException("BankAccountnot found"));
            AccountOperation accountOperation=new AccountOperation();
            accountOperation.setType(OperationType.CREDIT);
            accountOperation.setAmount(amount);
            accountOperation.setDescription(description);
            accountOperation.setOperationDate(new Date());
            accountOperation.setBankAccount(bankAccount);
            accountOperationRepository.save(accountOperation);
            bankAccount.setBalance(bankAccount.getBalance()+amount);
            bankAccountRepository.save(bankAccount);
        
    }
    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount)
            throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource,amount,"Transfer to "+accountIdDestination);
        credit(accountIdDestination,amount,"Transfer from"+accountIdSource);
    }
    @Override
    public List<BankAccountDTO> bankAccountList() {
        List<BankAccount> bankAccounts= bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS= bankAccounts.stream().map( 
            bankAccount-> {
                if (bankAccount instanceof SavingAccount) {
                    SavingAccount savingAccount= (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);
                } 
                else{
                    CurrentAccount currentAccount= (CurrentAccount) bankAccount;
                    return dtoMapper.fromCurrentBankAccount(currentAccount);
                }
            }).collect(Collectors.toList());
        return bankAccountDTOS;
    }
    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer= customerRepository.findById(customerId)
        .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        return dtoMapper.fromCustomer(customer);
    }
    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Savingnew Customer");
        Customer customer=dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer= customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }
    @Override
    public void deleteCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }
    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        List<AccountOperation> accountOperations= accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream().map(op -> dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
    }
    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size)
            throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElse(null);
        if(bankAccount==null) throw new BankAccountNotFoundException("Accountnot Found");
        Page<AccountOperation> accountOperations= accountOperationRepository.findByBankAccountId(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO=new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS= accountOperations.getContent().stream().map(op -> 
        dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
        
    }
}

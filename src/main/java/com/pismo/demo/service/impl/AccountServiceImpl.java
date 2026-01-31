package com.pismo.demo.service.impl;

import com.pismo.demo.dto.AccountResponseDTO;
import com.pismo.demo.entity.Account;
import com.pismo.demo.repository.AccountRepository;
import com.pismo.demo.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    @Transactional
    public AccountResponseDTO createAccount(String documentNumber) {
        log.info("create account for:{}", documentNumber);

        Account account = new Account();
        account.setDocumentNumber(documentNumber);
        try {
            Account savedAccount = accountRepository.save(account);
            log.info("account created for: {}", documentNumber);
            return new AccountResponseDTO(savedAccount.getId(), savedAccount.getDocumentNumber());
        } catch (Exception e) {
            log.error("FAILED to create account. Document Number: {} :: error {}", documentNumber, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccount(Long accountId) {
        log.info("get account details for: {}", accountId);
        try{
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));

            return new AccountResponseDTO(account.getId(), account.getDocumentNumber());
        }catch (Exception e) {
            log.error("FAILED to get account. Account accountId: {} :: error {}", accountId, e.getMessage());
            throw e;
        }
    }
}

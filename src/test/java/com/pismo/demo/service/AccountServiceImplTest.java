package com.pismo.demo.service;

import com.pismo.demo.dto.AccountResponseDTO;
import com.pismo.demo.entity.Account;
import com.pismo.demo.repository.AccountRepository;
import com.pismo.demo.service.impl.AccountServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;


    @Test
    @DisplayName("Create Account - Success")
    void createAccount_Success() {
        String documentNumber = "12345678900";
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setDocumentNumber(documentNumber);

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponseDTO result = accountService.createAccount(documentNumber);

        assertNotNull(result);
        assertEquals(1L, result.accountId());
        assertEquals(documentNumber, result.documentNumber());

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Create Account - Database Error (e.g., Duplicate Document)")
    void createAccount_DataIntegrityViolation() {
        String documentNumber = "12345678900";
        
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            accountService.createAccount(documentNumber);
        });

        assertEquals("Duplicate entry", exception.getMessage());
        verify(accountRepository, times(1)).save(any(Account.class));
    }


    @Test
    @DisplayName("Get Account - Success")
    void getAccount_Success() {
        Long accountId = 1L;
        String documentNumber = "12345678900";
        
        Account account = new Account();
        account.setId(accountId);
        account.setDocumentNumber(documentNumber);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountResponseDTO result = accountService.getAccount(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.accountId());
        assertEquals(documentNumber, result.documentNumber());
        
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    @DisplayName("Get Account - Not Found")
    void getAccount_NotFound() {
        Long accountId = 99L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.getAccount(accountId);
        });

        assertEquals("Account not found with ID: 99", exception.getMessage());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    @DisplayName("Get Account - Database Connection Error")
    void getAccount_DbError() {
        Long accountId = 1L;
        when(accountRepository.findById(accountId)).thenThrow(new RuntimeException("DB Connection Down"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getAccount(accountId);
        });

        assertEquals("DB Connection Down", exception.getMessage());
    }
}
package com.pismo.demo.service;

import com.pismo.demo.dto.TransactionRequestDTO;
import com.pismo.demo.dto.TransactionResponseDTO;
import com.pismo.demo.entity.Account;
import com.pismo.demo.entity.OperationType;
import com.pismo.demo.entity.Transaction;
import com.pismo.demo.repository.AccountRepository;
import com.pismo.demo.repository.OperationTypeRepository;
import com.pismo.demo.repository.TransactionRepository;
import com.pismo.demo.service.impl.TransactionServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OperationTypeRepository operationTypeRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;


    @Test
    @DisplayName("Create Transaction - Purchase (Debit) -> Amount should become negative")
    void createTransaction_DebitSuccess() {
        Long accountId = 1L;
        int operationTypeId = 1; // Normal Purchase (Negative)
        BigDecimal amount = new BigDecimal("100.00");

        TransactionRequestDTO request = new TransactionRequestDTO(accountId, operationTypeId, amount);

        Account account = new Account();
        account.setId(accountId);

        OperationType operationType = new OperationType();
        operationType.setId(1L);
        operationType.setSignMultiplier(-1); // DEBIT logic

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(1L)).thenReturn(Optional.of(operationType));
        
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(555L); // Simulate DB generating ID
            return t;
        });

        TransactionResponseDTO result = transactionService.createTransaction(request);

        assertNotNull(result);
        assertEquals(555L, result.transactionId());
        assertEquals(new BigDecimal("-100.00"), result.amount()); // Verified Negative
        
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Create Transaction - Payment (Credit) -> Amount should be positive")
    void createTransaction_CreditSuccess() {
        Long accountId = 1L;
        int operationTypeId = 4; // Credit Voucher (Positive)
        BigDecimal amount = new BigDecimal("50.00");

        TransactionRequestDTO request = new TransactionRequestDTO(accountId, operationTypeId, amount);

        Account account = new Account();
        account.setId(accountId);

        OperationType operationType = new OperationType();
        operationType.setId(4L);
        operationType.setSignMultiplier(1); // CREDIT logic

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(4L)).thenReturn(Optional.of(operationType));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDTO result = transactionService.createTransaction(request);

        assertEquals(new BigDecimal("50.00"), result.amount()); // Verified Positive
    }

    @Test
    @DisplayName("Create Transaction - Account Not Found -> Throw EntityNotFoundException")
    void createTransaction_AccountNotFound() {
        TransactionRequestDTO request = new TransactionRequestDTO(99L, 1, BigDecimal.TEN);

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
            transactionService.createTransaction(request)
        );

        assertEquals("Account not found", ex.getMessage());
        
        verify(operationTypeRepository, never()).findById(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Transaction - Invalid Operation Type -> Throw IllegalArgumentException")
    void createTransaction_InvalidOperationType() {
        TransactionRequestDTO request = new TransactionRequestDTO(1L, 99, BigDecimal.TEN);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(new Account()));
        when(operationTypeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            transactionService.createTransaction(request)
        );

        assertEquals("Invalid Operation Type ID", ex.getMessage());
        verify(transactionRepository, never()).save(any());
    }
}
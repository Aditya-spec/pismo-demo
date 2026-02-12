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
    @DisplayName("Create Transaction - Idempotency Hit (Key Exists) -> Return Existing Transaction")
    void createTransaction_IdempotencyHit() {
        // Arrange
        String idempotencyKey = "key-123";
        TransactionRequestDTO request = new TransactionRequestDTO(1L, 1, new BigDecimal("100.00"));

        // Simulate an existing transaction found in DB
        Transaction existingTx = new Transaction();
        existingTx.setId(999L);
        existingTx.setAccount(new Account()); // basic mock
        existingTx.getAccount().setId(1L);
        existingTx.setOperationTypeId(1);
        existingTx.setAmount(new BigDecimal("-100.00"));
        existingTx.setEventDate(LocalDateTime.now());
        existingTx.setIdempotencyKey(idempotencyKey);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingTx));

        TransactionResponseDTO result = transactionService.createTransaction(request, idempotencyKey);

        assertNotNull(result);
        assertEquals(999L, result.transactionId());
        assertEquals(new BigDecimal("-100.00"), result.amount());

        verify(accountRepository, never()).findById(any());
        verify(operationTypeRepository, never()).findById(any());
        verify(transactionRepository, never()).save(any());
    }


    @Test
    @DisplayName("Create Transaction - New Key (Success) -> Save and Return")
    void createTransaction_NewKey_Success() {
        String idempotencyKey = "key-new-456";
        TransactionRequestDTO request = new TransactionRequestDTO(1L, 1, new BigDecimal("100.00"));

        Account account = new Account();
        account.setId(1L);

        OperationType operationType = new OperationType();
        operationType.setId(1L);
        operationType.setSignMultiplier(-1);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(1L)).thenReturn(Optional.of(operationType));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(555L);
            return t;
        });


        TransactionResponseDTO result = transactionService.createTransaction(request, idempotencyKey);

        assertEquals(555L, result.transactionId());
        assertEquals(new BigDecimal("-100.00"), result.amount());
        verify(transactionRepository).save(any(Transaction.class));
    }



    @Test
    @DisplayName("Create Transaction - Account Not Found -> Throw EntityNotFoundException")
    void createTransaction_AccountNotFound() {
        // Arrange
        String key = "key-fail";
        TransactionRequestDTO request = new TransactionRequestDTO(99L, 1, BigDecimal.TEN);

        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                transactionService.createTransaction(request, key)
        );

        verify(transactionRepository, never()).save(any());
    }
}